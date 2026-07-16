package com.workbuddy.repository;

import com.workbuddy.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Optional<OperationLog> findByToolName(String toolName);

    /** 原子自增使用次数 */
    @Modifying
    @Query("update OperationLog o set o.useCount = o.useCount + 1, o.lastUsedAt = current_timestamp where o.toolName = :name")
    int increment(@Param("name") String name);

    /** 按分类统计总次数 */
    @Query("select coalesce(sum(o.useCount),0) from OperationLog o where o.toolCategory = :cat")
    long sumByCategory(@Param("cat") String cat);
}
