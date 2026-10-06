package com.opengym.repository;

import com.opengym.model.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserStateRepository extends JpaRepository<UserState, String> {
    @Modifying
    @Query("UPDATE UserState s SET s.stateJson = :stateJson, s.rev = :newRev, s.updatedAt = :updatedAt WHERE s.userId = :userId AND s.rev = :expectedRev")
    int updateStateWithRevCheck(String userId, String stateJson, int newRev, int expectedRev, String updatedAt);
}
