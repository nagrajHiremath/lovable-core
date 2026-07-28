package com.lovable.services.account_service.mapper;

import com.lovable.services.account_service.dto.subscription.SubscriptionResponse;
import com.lovable.services.account_service.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
  SubscriptionResponse toSubscriptionResponse(Subscription subscription);
}
