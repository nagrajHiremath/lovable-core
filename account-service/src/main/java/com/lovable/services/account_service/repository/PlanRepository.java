package com.lovable.services.account_service.repository;

import com.lovable.services.account_service.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
  Plan findByStripePriceId(String id);

  Optional<Plan> findByName(String name);
}
