package com.workbuddy.service;

import com.workbuddy.entity.UserPreference;
import com.workbuddy.repository.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户偏好 Service（按用户隔离：键值对：主题、默认城市等）
 */
@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final PreferenceRepository repository;

    /** 全部偏好（当前用户） */
    public Map<String, String> all(Long userId) {
        Map<String, String> map = new LinkedHashMap<>();
        for (UserPreference p : repository.findByUserId(userId)) {
            map.put(p.getKey(), p.getValue());
        }
        return map;
    }

    public String get(Long userId, String key) {
        return repository.findByUserIdAndKey(userId, key).map(UserPreference::getValue).orElse(null);
    }

    /** 设置/更新偏好（不存在则新建） */
    @Transactional
    public UserPreference set(Long userId, String key, String value, String category, String description) {
        Optional<UserPreference> opt = repository.findByUserIdAndKey(userId, key);
        UserPreference pref = opt.orElseGet(UserPreference::new);
        pref.setUserId(userId);
        pref.setKey(key);
        pref.setValue(value);
        if (category != null) pref.setCategory(category);
        if (description != null) pref.setDescription(description);
        return repository.save(pref);
    }

    public List<UserPreference> list(Long userId) {
        return repository.findByUserId(userId);
    }
}
