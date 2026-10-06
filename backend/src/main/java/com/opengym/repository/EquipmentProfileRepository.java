package com.opengym.repository;

import com.opengym.model.EquipmentProfile;
import com.opengym.model.UserEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentProfileRepository extends JpaRepository<EquipmentProfile, UserEntityId> {
    List<EquipmentProfile> findByUserId(String userId);
    Optional<EquipmentProfile> findByUserIdAndId(String userId, String id);
}
