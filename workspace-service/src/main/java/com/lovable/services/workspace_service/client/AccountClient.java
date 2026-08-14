package com.lovable.services.workspace_service.client;

import com.lovable.services.common_lib.dto.PlanResponse;
import com.lovable.services.common_lib.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "account-service", path = "/account", url="${ACCOUNT_SERVICE_URI:}")
public interface AccountClient {

    @GetMapping("/internal/v1/users/by-email")
    Optional<UserProfileResponse> getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/internal/v1/users/by-ids")
    List<UserProfileResponse> getUsersByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/internal/v1/billing/current-plan")
    PlanResponse getCurrentSubscribedPlanByUser(@RequestParam("userId") Long userId);
}
