package com.opengym.repository;

import com.opengym.model.SetDrop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetDropRepository extends JpaRepository<SetDrop, Long> {
    List<SetDrop> findBySetId(Long setId);
    List<SetDrop> findBySideId(Long sideId);
    List<SetDrop> findBySetIdOrderByDropOrder(Long setId);
    List<SetDrop> findBySideIdOrderByDropOrder(Long sideId);
}
