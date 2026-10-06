package com.opengym.service;

import com.opengym.model.User;
import com.opengym.model.UserState;
import com.opengym.repository.UserRepository;
import com.opengym.repository.UserStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final UserStateRepository stateRepo;
    private final JwtService jwtService;
    private final SettingsService settingsService;

    public UserService(UserRepository userRepo, UserStateRepository stateRepo,
                       JwtService jwtService, SettingsService settingsService) {
        this.userRepo = userRepo;
        this.stateRepo = stateRepo;
        this.jwtService = jwtService;
        this.settingsService = settingsService;
    }

    /** Register a new user. Returns JWT token. Throws if email already exists. */
    @Transactional
    public String register(String name, String email, String password) {
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String hash = hashPassword(password);

        User user = new User(id, name, email, hash);
        userRepo.save(user);

        UserState state = new UserState(id);
        stateRepo.save(state);

        // Create default user settings
        settingsService.createDefaults(id);

        return jwtService.generateToken(id);
    }

    /** Login with email + password. Returns JWT token or empty. */
    public Optional<String> login(String email, String password) {
        return userRepo.findByEmail(email)
                .filter(u -> !u.isDisabled())
                .filter(u -> u.getPasswordHash().equals(hashPassword(password)))
                .map(u -> jwtService.generateToken(u.getId()));
    }

    /** Find user by ID (from JWT subject). */
    public Optional<User> findById(String id) {
        return userRepo.findById(id).filter(u -> !u.isDisabled());
    }

    /** Authenticate from JWT token. Returns user or empty. */
    public Optional<User> authenticateJwt(String jwt) {
        String userId = jwtService.extractUserId(jwt);
        if (userId == null) return Optional.empty();
        return findById(userId);
    }

    public long userCount() {
        return userRepo.count();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hash failed", e);
        }
    }
}
