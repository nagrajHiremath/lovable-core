package com.lovable.services.account_service.service.impl;

import com.lovable.services.account_service.dto.subscription.CheckoutRequest;
import com.lovable.services.account_service.dto.subscription.CheckoutResponse;
import com.lovable.services.account_service.dto.subscription.PortalResponse;
import com.lovable.services.account_service.entity.Plan;
import com.lovable.services.account_service.entity.User;
import com.lovable.services.account_service.repository.PlanRepository;
import com.lovable.services.account_service.repository.UserRepository;
import com.lovable.services.account_service.service.PaymentProcessor;
import com.lovable.services.account_service.service.SubscriptionService;
import com.lovable.services.common_lib.enums.SubscriptionStatus;
import com.lovable.services.common_lib.security.AuthUtil;
import com.stripe.exception.StripeException;

import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import com.stripe.model.*;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {

  private final AuthUtil authUtil;
  private final PlanRepository planRepository;
  private final UserRepository userRepository;
  private final SubscriptionService subscriptionService;

  // Reads the FRONTEND_URL env var directly (with an inline default) rather than
  // through common-config's app.frontend.url, whose k8s-profile value is itself
  // "${FRONTEND_URL}" with no default of its own - a default on this annotation
  // alone would NOT have prevented the resulting PlaceholderResolutionException,
  // since the outer property already resolves to that unresolvable nested
  // reference. Going straight to the env var avoids depending on that chain at all.
  @Value("${FRONTEND_URL:http://34.14.138.43}")
  private String frontEndUrl;

  @Override
  public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest checkoutRequest) {

    Long userId = authUtil.getCurrentUserId();
    Plan plan = planRepository.findById(checkoutRequest.planId()).orElseThrow();
    User user = getUser(userId);

    var params =
        SessionCreateParams.builder()
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(plan.getStripePriceId())
                    .setQuantity(1L)
                    .build())
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSubscriptionData(
                new SessionCreateParams.SubscriptionData.Builder()
                    .setBillingMode(
                        SessionCreateParams.SubscriptionData.BillingMode.builder()
                            .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                            .build())
                    .build())
            .setSuccessUrl(frontEndUrl + "/projects?checkout=success&session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(frontEndUrl + "/projects?checkout=cancelled")
            .putMetadata("user_id", user.getId().toString())
            .putMetadata("plan_id", plan.getId().toString());

    try {
      String stripeCustomerId = user.getStripeCustomerId();
      if (stripeCustomerId == null) {
        params.setCustomerEmail(user.getUsername());
      } else {
        params.setCustomer(stripeCustomerId);
      }

      Session session = Session.create(params.build());
      return new CheckoutResponse(session.getUrl());
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PortalResponse openCustomerPortal() {
    User user = getUser(authUtil.getCurrentUserId());
    String stripeCustomerId = user.getStripeCustomerId();

    if (stripeCustomerId == null) {
      throw new IllegalArgumentException("User does not have a Stripe customer ID");
    }

    try {
      var portalSession =
          com.stripe.model.billingportal.Session.create(
              com.stripe.param.billingportal.SessionCreateParams.builder()
                  .setCustomer(stripeCustomerId)
                  .setReturnUrl(frontEndUrl)
                  .build());

      return new PortalResponse(portalSession.getUrl());
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void handleWebhookEvent(
          String type, StripeObject stripeObject, Map<String, String> metadata) {
    switch (type) {
      case "checkout.session.completed" ->
          handleCheckoutSessionComplete((Session) stripeObject, metadata);
      case "customer.subscription.updated" ->
          handleCustomerSubscriptionUpdated((Subscription) stripeObject);
      case "customer.subscription.deleted" ->
          handleCustomerSubscriptionDeleted((Subscription) stripeObject);
      case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject);
      case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject);
      default -> log.debug("ignoring event {}", type);
    }
  }

  private void handleCheckoutSessionComplete(Session session, Map<String, String> metadata) {

    Long userId = Long.parseLong(metadata.get("user_id"));
    Long planId = Long.parseLong(metadata.get("plan_id"));

    String stripeSubscriptionId = session.getSubscription();
    String stripeCustomerId = session.getCustomer();

    User user = getUser(userId);
    if (user.getStripeCustomerId() == null) {
      user.setStripeCustomerId(stripeCustomerId);
      userRepository.save(user);
    }

    subscriptionService.activateSubscription(userId, planId, stripeSubscriptionId, stripeCustomerId);
  }

  private void handleCustomerSubscriptionUpdated(Subscription subscription) {
    if (subscription == null) {
      log.error("Failed to update customer subscription: subscription is null");
    }

    SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());

    SubscriptionItem item = subscription.getItems().getData().get(0);

    Instant periodStart = getInstant(item.getCurrentPeriodStart());
    Instant periodEnd = getInstant(item.getCurrentPeriodEnd());

    Long planId = resolvePlanId(item.getPrice());

    subscriptionService.updateSubscription(
        subscription.getId(),
        status,
        periodStart,
        periodEnd,
        subscription.getCancelAtPeriodEnd(),
        planId);
  }

  private void handleCustomerSubscriptionDeleted(Subscription subscription) {
    if (subscription == null) {
      log.error("Failed to delete customer subscription: subscription is null");
    }

    subscriptionService.cancelSubscription(subscription.getId());
  }

  private void handleInvoicePaid(Invoice invoice) {
    String subId = getSubscriptionId(invoice);
    if (subId == null) {
      return;
    }

    try {
      Subscription subscription = Subscription.retrieve(subId);
      var item = subscription.getItems().getData().get(0);

      Instant periodStart = getInstant(item.getCurrentPeriodStart());
      Instant periodEnd = getInstant(item.getCurrentPeriodEnd());

      subscriptionService.renewSubscriptionPeriod(subId, periodStart, periodEnd);
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleInvoicePaymentFailed(Invoice invoice) {
    if (invoice == null) {
      log.error("Failed to handle invoice payment failed: invoice is null");
      return;
    }
    subscriptionService.markSubscriptionPastDue(getSubscriptionId(invoice));
  }

  /*
   * Utility methods
   * */
  private String getSubscriptionId(Invoice invoice) {
    var parent = invoice.getParent();
    if (parent == null) {
      log.error("Failed to get subscription ID: parent is null");
      return null;
    }
    var subscriptionDetails = parent.getSubscriptionDetails();
    if (subscriptionDetails == null) {
      log.error("Failed to get subscription ID: subscription details are null");
      return null;
    }
    return subscriptionDetails.getSubscription();
  }

  private @NonNull User getUser(Long userId) {
    return userRepository.findById(userId).orElseThrow();
  }

  private SubscriptionStatus mapStripeStatusToEnum(String status) {
    return switch (status) {
      case "active" -> SubscriptionStatus.ACTIVE;
      case "trialing" -> SubscriptionStatus.TRIALING;
      case "canceled" -> SubscriptionStatus.CANCELED;
      case "past_due" -> SubscriptionStatus.PAST_DUE;
      case "incomplete" -> SubscriptionStatus.INCOMPLETE;
      default -> throw new IllegalArgumentException("Unknown subscription status: " + status);
    };
  }

  private static Instant getInstant(Long timestamp) {
    return Instant.ofEpochSecond(timestamp);
  }

  private Long resolvePlanId(Price price) {
    return planRepository.findByStripePriceId(price.getId()).getId();
  }
}
