package com.opengym.service;

import com.opengym.model.CustomExercise;
import com.opengym.repository.CustomExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class CustomExerciseService {

    private final CustomExerciseRepository repo;

    public CustomExerciseService(CustomExerciseRepository repo) {
        this.repo = repo;
    }

    public List<CustomExercise> listExercises(String userId) {
        return repo.findByUserId(userId);
    }

    @Transactional
    public CustomExercise createExercise(String userId, Map<String, Object> body) {
        CustomExercise ce = new CustomExercise();
        ce.setUserId(userId);
        ce.setId(body.containsKey("id") ? (String) body.get("id")
                : UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        ce.setName((String) body.getOrDefault("name", "Custom Exercise"));
        ce.setBodyPart((String) body.get("bodyPart"));
        ce.setEquipment((String) body.get("equipment"));
        ce.setTarget((String) body.get("target"));
        ce.setDescription((String) body.get("description"));
        ce.setCreatedAt(Instant.now().toString());
        return repo.save(ce);
    }

    @Transactional
    public CustomExercise updateExercise(String userId, String exerciseId, Map<String, Object> body) {
        Optional<CustomExercise> opt = repo.findByUserIdAndId(userId, exerciseId);
        if (opt.isEmpty()) return null;

        CustomExercise ce = opt.get();
        if (body.containsKey("name"))        ce.setName((String) body.get("name"));
        if (body.containsKey("bodyPart"))    ce.setBodyPart((String) body.get("bodyPart"));
        if (body.containsKey("equipment"))   ce.setEquipment((String) body.get("equipment"));
        if (body.containsKey("target"))      ce.setTarget((String) body.get("target"));
        if (body.containsKey("description")) ce.setDescription((String) body.get("description"));
        return repo.save(ce);
    }

    @Transactional
    public boolean deleteExercise(String userId, String exerciseId) {
        Optional<CustomExercise> opt = repo.findByUserIdAndId(userId, exerciseId);
        if (opt.isEmpty()) return false;
        repo.delete(opt.get());
        return true;
    }
}
