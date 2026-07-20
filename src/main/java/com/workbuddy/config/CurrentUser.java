package com.workbuddy.config;

import com.workbuddy.entity.User;
import com.workbuddy.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 便捷获取当前登录用户。在 Controller / Service 中注入即可。
 * 优先从 SecurityUser 直接取 userId（无需查库）。
 */
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 当前登录用户 id；未登录返回 null */
    public Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof SecurityUser) {
            return ((SecurityUser) principal).getUserId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).map(User::getId).orElse(null);
        }
        return null;
    }

    /** 当前登录用户实体；未登录返回 empty */
    public Optional<User> getUser() {
        Long uid = getUserId();
        if (uid == null) {
            return Optional.empty();
        }
        return userRepository.findById(uid);
    }
}
