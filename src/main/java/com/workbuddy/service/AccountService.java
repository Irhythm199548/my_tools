package com.workbuddy.service;

import com.workbuddy.common.BizException;
import com.workbuddy.config.CurrentUser;
import com.workbuddy.entity.User;
import com.workbuddy.entity.VerificationCode;
import com.workbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 账号相关业务：注册、找回密码、修改密码、个人资料、头像、工具配置。
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;
    private final ToolCatalog toolCatalog;

    @Value("${mytools.upload.dir:./uploads}")
    private String uploadDir;

    private static final long MAX_AVATAR_BYTES = 2 * 1024 * 1024;
    private static final List<String> ALLOWED_EXT = Arrays.asList("png", "jpg", "jpeg", "gif", "webp");

    // ============ 注册 ============

    @Transactional
    public User register(String username, String email, String code, String password, String nickname) {
        validateUsername(username);
        validateEmail(email);
        validatePassword(password);
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new BizException("昵称为必填项");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BizException("该用户名已被占用");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BizException("该邮箱已被注册");
        }
        verificationCodeService.verify(email, code, VerificationCode.Type.REGISTER);

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname.trim());
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    // ============ 找回密码 ============

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        validateEmail(email);
        validatePassword(newPassword);
        verificationCodeService.verify(email, code, VerificationCode.Type.RESET);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BizException("该邮箱尚未注册"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ============ 修改密码（已登录） ============

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        Long uid = currentUser.getUserId();
        if (uid == null) {
            throw new BizException("请先登录");
        }
        validatePassword(newPassword);
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new BizException("账号不存在"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ============ 资料 ============

    public User getProfile() {
        return currentUser.getUser().orElseThrow(() -> new BizException("请先登录"));
    }

    @Transactional
    public User updateProfile(String nickname, String bio, String phone,
                              String location, String website, String company, Date birthday) {
        User user = getProfile();
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new BizException("昵称为必填项");
        }
        user.setNickname(nickname.trim());
        user.setBio(bio);
        user.setPhone(phone);
        user.setLocation(location);
        user.setWebsite(website);
        user.setCompany(company);
        user.setBirthday(birthday);
        return userRepository.save(user);
    }

    // ============ 头像 ============

    @Transactional
    public String updateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择头像文件");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BizException("头像文件不能超过 2MB");
        }
        String original = file.getOriginalFilename();
        String ext = original == null ? "png" : original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持 png/jpg/jpeg/gif/webp 格式");
        }
        try {
            Path dir = Paths.get(uploadDir, "avatars").toAbsolutePath();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            String url = "/uploads/avatars/" + filename;
            User user = getProfile();
            user.setAvatar(url);
            userRepository.save(user);
            return url;
        } catch (Exception e) {
            throw new BizException("头像上传失败：" + e.getMessage());
        }
    }

    // ============ 工具配置 ============

    public List<String> getEnabledTools() {
        User user = getProfile();
        return toolCatalog.resolveEnabled(user.getEnabledTools());
    }

    @Transactional
    public List<String> updateEnabledTools(List<String> enabled) {
        User user = getProfile();
        List<String> valid = toolCatalog.allKeys();
        StringBuilder sb = new StringBuilder();
        if (enabled != null) {
            for (String k : enabled) {
                if (valid.contains(k)) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(k);
                }
            }
        }
        user.setEnabledTools(sb.length() == 0 ? null : sb.toString());
        userRepository.save(user);
        return toolCatalog.resolveEnabled(user.getEnabledTools());
    }

    // ============ 校验工具 ============

    private void validateUsername(String username) {
        if (username == null || !username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new BizException("用户名需为 3-20 位字母/数字/下划线");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BizException("邮箱格式不正确");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 64) {
            throw new BizException("密码长度需为 6-64 位");
        }
    }
}
