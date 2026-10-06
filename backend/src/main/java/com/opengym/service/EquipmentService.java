package com.opengym.service;

import com.opengym.model.EquipmentProfile;
import com.opengym.model.EquipmentProfileItem;
import com.opengym.repository.EquipmentProfileItemRepository;
import com.opengym.repository.EquipmentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class EquipmentService {

    private final EquipmentProfileRepository profileRepo;
    private final EquipmentProfileItemRepository itemRepo;

    public EquipmentService(EquipmentProfileRepository profileRepo, EquipmentProfileItemRepository itemRepo) {
        this.profileRepo = profileRepo;
        this.itemRepo = itemRepo;
    }

    /** List all profiles with their items. */
    public List<Map<String, Object>> listProfiles(String userId) {
        List<EquipmentProfile> profiles = profileRepo.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (EquipmentProfile p : profiles) {
            result.add(profileToMap(userId, p));
        }
        return result;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> createProfile(String userId, Map<String, Object> body) {
        String id = body.containsKey("id") ? (String) body.get("id")
                : UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        EquipmentProfile p = new EquipmentProfile();
        p.setUserId(userId);
        p.setId(id);
        p.setName((String) body.getOrDefault("name", "New Profile"));
        profileRepo.save(p);

        // Save items
        if (body.containsKey("items") && body.get("items") instanceof List) {
            List<String> items = (List<String>) body.get("items");
            for (String eq : items) {
                EquipmentProfileItem item = new EquipmentProfileItem();
                item.setUserId(userId);
                item.setProfileId(id);
                item.setEquipment(eq);
                itemRepo.save(item);
            }
        }

        return profileToMap(userId, p);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> updateProfile(String userId, String profileId, Map<String, Object> body) {
        Optional<EquipmentProfile> opt = profileRepo.findByUserIdAndId(userId, profileId);
        if (opt.isEmpty()) return null;

        EquipmentProfile p = opt.get();
        if (body.containsKey("name")) p.setName((String) body.get("name"));
        profileRepo.save(p);

        // Replace items if provided
        if (body.containsKey("items") && body.get("items") instanceof List) {
            List<EquipmentProfileItem> oldItems = itemRepo.findByUserIdAndProfileId(userId, profileId);
            itemRepo.deleteAll(oldItems);

            List<String> items = (List<String>) body.get("items");
            for (String eq : items) {
                EquipmentProfileItem item = new EquipmentProfileItem();
                item.setUserId(userId);
                item.setProfileId(profileId);
                item.setEquipment(eq);
                itemRepo.save(item);
            }
        }

        return profileToMap(userId, p);
    }

    @Transactional
    public boolean deleteProfile(String userId, String profileId) {
        Optional<EquipmentProfile> opt = profileRepo.findByUserIdAndId(userId, profileId);
        if (opt.isEmpty()) return false;

        List<EquipmentProfileItem> items = itemRepo.findByUserIdAndProfileId(userId, profileId);
        itemRepo.deleteAll(items);
        profileRepo.delete(opt.get());
        return true;
    }

    private Map<String, Object> profileToMap(String userId, EquipmentProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        List<EquipmentProfileItem> items = itemRepo.findByUserIdAndProfileId(userId, p.getId());
        List<String> equipmentList = new ArrayList<>();
        for (EquipmentProfileItem item : items) equipmentList.add(item.getEquipment());
        m.put("items", equipmentList);
        return m;
    }
}
