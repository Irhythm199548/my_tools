package com.workbuddy.service;

import com.workbuddy.entity.OperationLog;
import com.workbuddy.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 操作日志 Service：记录各工具使用次数（用于统计）
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository repository;

    /** 记录一次工具使用（不存在则插入，存在则原子自增） */
    @Transactional
    public void record(String toolName, String category) {
        int affected = repository.increment(toolName);
        if (affected == 0) {
            OperationLog log = new OperationLog();
            log.setToolName(toolName);
            log.setToolCategory(category);
            log.setUseCount(1L);
            log.setLastUsedAt(new Date());
            repository.save(log);
        }
    }

    /** 使用统计排行（按次数降序） */
    public List<OperationLog> stats() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "useCount"));
    }

    /** 某分类累计使用次数 */
    public long totalByCategory(String category) {
        return repository.sumByCategory(category);
    }
}
