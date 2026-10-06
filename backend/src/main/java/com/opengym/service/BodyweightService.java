package com.opengym.service;

import com.opengym.model.BodyweightEntry;
import com.opengym.repository.BodyweightEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class BodyweightService {

    private final BodyweightEntryRepository repo;

    public BodyweightService(BodyweightEntryRepository repo) {
        this.repo = repo;
    }

    /** Returns entries in frontend format: { id, d, w, t } */
    public List<Map<String, Object>> listEntries(String userId) {
        List<BodyweightEntry> entries = repo.findByUserIdOrderByTimestampDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (BodyweightEntry e : entries) {
            result.add(toFrontendMap(e));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> addEntry(String userId, Map<String, Object> body) {
        BodyweightEntry e = new BodyweightEntry();
        e.setUserId(userId);
        // Frontend sends "d" for date, "w" for weight, "t" for timestamp
        String dateVal = (String) body.getOrDefault("date",
                body.getOrDefault("d",
                        body.getOrDefault("dateIso", Instant.now().toString().substring(0, 10))));
        e.setDateIso(dateVal);
        e.setWeight(((Number) body.get("weight")).doubleValue());
        Object ts = body.containsKey("t") ? body.get("t") : body.get("timestamp");
        if (ts instanceof Number) e.setTimestamp(((Number) ts).longValue());
        e.setCreatedAt(Instant.now().toString());
        BodyweightEntry saved = repo.save(e);
        return toFrontendMap(saved);
    }

    @Transactional
    public boolean deleteEntry(String userId, Long id) {
        Optional<BodyweightEntry> opt = repo.findById(id);
        if (opt.isEmpty() || !opt.get().getUserId().equals(userId)) return false;
        repo.delete(opt.get());
        return true;
    }

    private Map<String, Object> toFrontendMap(BodyweightEntry e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("d", e.getDateIso());
        m.put("w", e.getWeight());
        m.put("t", e.getTimestamp());
        return m;
    }
}
