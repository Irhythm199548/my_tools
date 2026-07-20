package com.workbuddy.repository;

import com.workbuddy.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /** 查询某邮箱某类型最新且未使用的一条 */
    Optional<VerificationCode> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, VerificationCode.Type type);

    /** 清理过期验证码（定时/发送前调用） */
    long deleteByExpiresAtBefore(Date before);

    List<VerificationCode> findByEmailAndTypeOrderByCreatedAtDesc(String email, VerificationCode.Type type);
}
