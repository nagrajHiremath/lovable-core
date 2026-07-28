package com.lovable.services.account_service.repository;

import com.lovable.services.account_service.entity.Subscription;
import com.lovable.services.common_lib.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
  Optional<Subscription> findByUserId(Long userId);

  Optional<Subscription> findByUserIdAndStatusIn(Long userId, Set<SubscriptionStatus> active);

  Optional<Subscription> findByStripeSubscriptionId(String gatewaySubscriptionId);

  boolean existsByStripeSubscriptionId(String stripeSubscriptionId);
}
