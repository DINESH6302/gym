package com.opengym.repository;

import com.opengym.model.SetCluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetClusterRepository extends JpaRepository<SetCluster, Long> {
    List<SetCluster> findBySetId(Long setId);
    List<SetCluster> findBySideId(Long sideId);
    List<SetCluster> findBySetIdOrderByClusterOrder(Long setId);
    List<SetCluster> findBySideIdOrderByClusterOrder(Long sideId);
}
