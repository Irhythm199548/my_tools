package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.config.CurrentUser;
import com.workbuddy.entity.User;
import com.workbuddy.entity.VerificationCode;
import com.workbuddy.service.AccountService;
import com.workbuddy.service.VerificationCodeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 账号相关接口：注册、发码、验证码校验、找回密码、改密、资料、头像、工具配置。
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
    private final VerificationCodeService verificationCodeService;
    private final CurrentUser currentUser;

    public AccountController(AccountService accountService,
                              VerificationCodeService verificationCodeService,
                              CurrentUser currentUser) {
        this.accountService = accountService;
        this.verificationCodeService = verificationCodeService;
        this.currentUser = currentUser;
    }

    /** 发送验证码（type=REGISTER|RESET） */
    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String type = body.get("type");
        VerificationCode.Type t = "RESET".equalsIgnoreCase(type) ? VerificationCode.Type.RESET : VerificationCode.Type.REGISTER;
        verificationCodeService.send(email, t);
        return ApiResponse.success();
    }

    /** 校验验证码（注册/找回流程中可选前置校验） */
    @PostMapping("/verify-code")
    public ApiResponse<Void> verifyCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String type = body.get("type");
        VerificationCode.Type t = "RESET".equalsIgnoreCase(type) ? VerificationCode.Type.RESET : VerificationCode.Type.REGISTER;
        verificationCodeService.verify(email, code, t);
        return ApiResponse.success();
    }

    /** 注册 */
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody Map<String, String> body) {
        accountService.register(
                body.get("username"),
                body.get("email"),
                body.get("code"),
                body.get("password"),
                body.get("nickname"));
        return ApiResponse.success();
    }

    /** 找回密码（邮箱验证码重置） */
    @PostMapping("/forgot/reset")
    public ApiResponse<Void> reset(@RequestBody Map<String, String> body) {
        accountService.resetPassword(body.get("email"), body.get("code"), body.get("newPassword"));
        return ApiResponse.success();
    }

    /** 修改密码（已登录，需原密码） */
    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        accountService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return ApiResponse.success();
    }

    /** 当前用户资料 */
    @GetMapping("/profile")
    public ApiResponse<User> profile() {
        return ApiResponse.success(accountService.getProfile());
    }

    /** 更新资料 */
    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@RequestBody Map<String, Object> body) {
        String birthdayStr = body.get("birthday") == null ? null : String.valueOf(body.get("birthday"));
        Date birthday = null;
        if (birthdayStr != null && !birthdayStr.isEmpty()) {
            try {
                birthday = new SimpleDateFormat("yyyy-MM-dd").parse(birthdayStr);
            } catch (ParseException e) {
                return ApiResponse.error(400, "生日格式应为 yyyy-MM-dd");
            }
        }
        User user = accountService.updateProfile(
                (String) body.get("nickname"),
                (String) body.get("bio"),
                (String) body.get("phone"),
                (String) body.get("location"),
                (String) body.get("website"),
                (String) body.get("company"),
                birthday);
        return ApiResponse.success(user);
    }

    /** 上传头像 */
    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> avatar(@RequestParam("file") MultipartFile file) {
        String url = accountService.updateAvatar(file);
        Map<String, String> m = new java.util.HashMap<>();
        m.put("url", url);
        return ApiResponse.success(m);
    }

    /** 当前用户启用的工具 */
    @GetMapping("/tools")
    public ApiResponse<List<String>> tools() {
        return ApiResponse.success(accountService.getEnabledTools());
    }

    /** 设置启用的工具 */
    @PutMapping("/tools")
    public ApiResponse<List<String>> updateTools(@RequestBody Map<String, List<String>> body) {
        List<String> enabled = body.get("enabled");
        return ApiResponse.success(accountService.updateEnabledTools(enabled));
    }
}
