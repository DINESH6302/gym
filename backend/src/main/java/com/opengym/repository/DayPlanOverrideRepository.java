package com.opengym.repository;

import com.opengym.model.DayPlanOverride;
import com.opengym.model.DayPlanOverrideId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DayPlanOverrideRepository extends JpaRepository<DayPlanOverride, DayPlanOverrideId> {
    List<DayPlanOverride> findByUserId(String userId);
    Optional<DayPlanOverride> findByUserIdAndDateIso(String userId, String dateIso);
}
