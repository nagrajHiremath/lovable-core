package com.lovable.services.account_service.service;

import com.lovable.services.account_service.dto.subscription.CheckoutRequest;
import com.lovable.services.account_service.dto.subscription.CheckoutResponse;
import com.lovable.services.account_service.dto.subscription.PortalResponse;
import com.stripe.model.StripeObject;
import java.util.Map;

public interface PaymentProcessor {
  CheckoutResponse createCheckoutSessionUrl(CheckoutRequest checkoutRequest);

  PortalResponse openCustomerPortal();

  void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata);
}
