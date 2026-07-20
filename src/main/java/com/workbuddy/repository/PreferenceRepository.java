package com.workbuddy.repository;

import com.workbuddy.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreferenceRepository extends JpaRepository<UserPreference, Long> {

    Optional<UserPreference> findByUserIdAndKey(Long userId, String key);

    List<UserPreference> findByUserId(Long userId);
}
