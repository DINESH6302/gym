package com.opengym.service;

import com.opengym.model.BarWeight;
import com.opengym.model.ExerciseNote;
import com.opengym.model.ExerciseWeight;
import com.opengym.model.FavouriteExercise;
import com.opengym.model.UserExerciseId;
import com.opengym.repository.BarWeightRepository;
import com.opengym.repository.ExerciseNoteRepository;
import com.opengym.repository.ExerciseWeightRepository;
import com.opengym.repository.FavouriteExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExerciseDataService {

    private final ExerciseWeightRepository weightRepo;
    private final ExerciseNoteRepository noteRepo;
    private final FavouriteExerciseRepository favRepo;
    private final BarWeightRepository barRepo;

    public ExerciseDataService(ExerciseWeightRepository weightRepo,
                               ExerciseNoteRepository noteRepo,
                               FavouriteExerciseRepository favRepo,
                               BarWeightRepository barRepo) {
        this.weightRepo = weightRepo;
        this.noteRepo = noteRepo;
        this.favRepo = favRepo;
        this.barRepo = barRepo;
    }

    // ---- Exercise Weights ----
    // Frontend expects: { "bench-press": { w: 100, d: "2024-01-15" }, ... }

    public Map<String, Object> getAllWeightsMap(String userId) {
        List<ExerciseWeight> weights = weightRepo.findByUserId(userId);
        Map<String, Object> map = new LinkedHashMap<>();
        for (ExerciseWeight ew : weights) {
            Map<String, Object> val = new LinkedHashMap<>();
            val.put("w", ew.getWeight());
            map.put(ew.getExerciseId(), val);
        }
        return map;
    }

    @Transactional
    public ExerciseWeight setWeight(String userId, String exerciseId, Map<String, Object> body) {
        ExerciseWeight ew = weightRepo.findByUserIdAndExerciseId(userId, exerciseId)
                .orElse(new ExerciseWeight());
        ew.setUserId(userId);
        ew.setExerciseId(exerciseId);
        ew.setWeight(((Number) body.get("weight")).doubleValue());
        return weightRepo.save(ew);
    }

    // ---- Exercise Notes ----
    // Frontend expects: { "bench-press": "focus on form", ... }

    public Map<String, String> getAllNotesMap(String userId) {
        List<ExerciseNote> notes = noteRepo.findByUserId(userId);
        Map<String, String> map = new LinkedHashMap<>();
        for (ExerciseNote en : notes) {
            map.put(en.getExerciseId(), en.getNote());
        }
        return map;
    }

    @Transactional
    public ExerciseNote setNote(String userId, String exerciseId, Map<String, Object> body) {
        ExerciseNote en = noteRepo.findByUserIdAndExerciseId(userId, exerciseId)
                .orElse(new ExerciseNote());
        en.setUserId(userId);
        en.setExerciseId(exerciseId);
        en.setNote((String) body.get("note"));
        return noteRepo.save(en);
    }

    // ---- Favourites ----

    public List<String> getFavourites(String userId) {
        return favRepo.findByUserIdOrderBySortOrder(userId).stream()
                .map(FavouriteExercise::getExerciseId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavourite(String userId, String exerciseId) {
        UserExerciseId pk = new UserExerciseId(userId, exerciseId);
        if (favRepo.existsById(pk)) return;

        List<FavouriteExercise> existing = favRepo.findByUserId(userId);
        int maxSort = existing.stream().mapToInt(FavouriteExercise::getSortOrder).max().orElse(-1);

        FavouriteExercise fav = new FavouriteExercise();
        fav.setUserId(userId);
        fav.setExerciseId(exerciseId);
        fav.setSortOrder(maxSort + 1);
        favRepo.save(fav);
    }

    @Transactional
    public boolean removeFavourite(String userId, String exerciseId) {
        UserExerciseId pk = new UserExerciseId(userId, exerciseId);
        if (!favRepo.existsById(pk)) return false;
        favRepo.deleteById(pk);
        return true;
    }

    // ---- Bar Weights ----
    // Frontend expects: { "bench-press": 20, ... }

    public Map<String, Double> getAllBarWeightsMap(String userId) {
        List<BarWeight> weights = barRepo.findByUserId(userId);
        Map<String, Double> map = new LinkedHashMap<>();
        for (BarWeight bw : weights) {
            map.put(bw.getExerciseId(), bw.getWeight());
        }
        return map;
    }

    @Transactional
    public BarWeight setBarWeight(String userId, String exerciseId, Map<String, Object> body) {
        BarWeight bw = barRepo.findByUserIdAndExerciseId(userId, exerciseId)
                .orElse(new BarWeight());
        bw.setUserId(userId);
        bw.setExerciseId(exerciseId);
        bw.setWeight(((Number) body.get("weight")).doubleValue());
        return barRepo.save(bw);
    }
}
