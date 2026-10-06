package com.opengym.repository;

import com.opengym.model.BodyweightEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BodyweightEntryRepository extends JpaRepository<BodyweightEntry, Long> {
    List<BodyweightEntry> findByUserId(String userId);
    List<BodyweightEntry> findByUserIdAndDateIso(String userId, String dateIso);
    List<BodyweightEntry> findByUserIdOrderByTimestampDesc(String userId);
}
