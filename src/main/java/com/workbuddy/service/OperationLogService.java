package com.workbuddy.service;

import com.workbuddy.common.BizException;
import com.workbuddy.config.CurrentUser;
import com.workbuddy.entity.OperationLog;
import com.workbuddy.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 操作日志 Service：记录各工具使用次数（按用户隔离，用于统计）
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository repository;
    private final CurrentUser currentUser;

    /** 记录一次工具使用（不存在则插入，存在则原子自增）。按当前登录用户隔离。 */
    @Transactional
    public void record(String toolName, String category) {
        Long userId = currentUser.getUserId();
        if (userId == null) {
            // 未登录（理论上不会走到，因为接口已鉴权）；降级为不记录
            return;
        }
        int affected = repository.increment(userId, toolName);
        if (affected == 0) {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setToolName(toolName);
            log.setToolCategory(category);
            log.setUseCount(1L);
            log.setLastUsedAt(new Date());
            repository.save(log);
        }
    }

    /** 使用统计排行（按当前用户，次数降序） */
    public List<OperationLog> stats() {
        Long userId = currentUser.getUserId();
        if (userId == null) throw new BizException("请先登录");
        return repository.findByUserIdOrderByUseCountDesc(userId);
    }

    /** 某分类累计使用次数（当前用户） */
    public long totalByCategory(String category) {
        Long userId = currentUser.getUserId();
        if (userId == null) throw new BizException("请先登录");
        return repository.sumByCategory(userId, category);
    }
}
