package com.lovable.services.account_service.config;

import com.lovable.services.account_service.entity.Plan;
import com.lovable.services.account_service.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlanSeeder implements ApplicationRunner {

  private final PlanRepository planRepository;

  @Override
  public void run(ApplicationArguments args) {
    if (planRepository.count() > 0) {
      return;
    }

    log.info("No plans found, seeding default plans");

    planRepository.saveAll(
        List.of(
            Plan.builder()
                .name("Free")
                .price("$0")
                .stripePriceId("price_1TrbAGKibACKXiAvnH81P8FS")
                .maxProjects(3)
                .maxTokensPerDay(20000)
                .maxPreviews(3)
                .unlimitedAI(false)
                .isActive(true)
                .build(),
            Plan.builder()
                .name("Pro")
                .price("$29")
                .stripePriceId("price_1TrbAwKibACKXiAvihAlt8t7")
                .maxProjects(20)
                .maxTokensPerDay(300000)
                .maxPreviews(20)
                .unlimitedAI(true)
                .isActive(true)
                .build(),
            Plan.builder()
                .name("Business")
                .price("$99")
                .stripePriceId("price_1TrbBWKibACKXiAvmlj7Gs2D")
                .maxProjects(1000)
                .maxTokensPerDay(2000000)
                .maxPreviews(100)
                .unlimitedAI(true)
                .isActive(true)
                .build()));
  }
}
