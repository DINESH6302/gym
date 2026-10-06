package com.opengym.repository;

import com.opengym.model.EquipmentProfileItem;
import com.opengym.model.EquipmentProfileItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentProfileItemRepository extends JpaRepository<EquipmentProfileItem, EquipmentProfileItemId> {
    List<EquipmentProfileItem> findByUserId(String userId);
    List<EquipmentProfileItem> findByUserIdAndProfileId(String userId, String profileId);
}
