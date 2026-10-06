package com.opengym.repository;

import com.opengym.model.SetSide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SetSideRepository extends JpaRepository<SetSide, Long> {
    List<SetSide> findBySetId(Long setId);
    Optional<SetSide> findBySetIdAndSide(Long setId, String side);
}
