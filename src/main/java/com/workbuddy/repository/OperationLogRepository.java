package com.workbuddy.repository;

import com.workbuddy.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Optional<OperationLog> findByUserIdAndToolName(Long userId, String toolName);

    /** 原子自增某用户某工具的使用次数；返回受影响行数（0 表示记录尚不存在） */
    @Modifying
    @Query("update OperationLog o set o.useCount = o.useCount + 1, o.lastUsedAt = current_timestamp " +
            "where o.userId = :userId and o.toolName = :name")
    int increment(@Param("userId") Long userId, @Param("name") String name);

    /** 按分类统计某用户总次数 */
    @Query("select coalesce(sum(o.useCount),0) from OperationLog o where o.userId = :userId and o.toolCategory = :cat")
    long sumByCategory(@Param("userId") Long userId, @Param("cat") String cat);

    /** 某用户使用统计排行（按次数降序） */
    @Query("select o from OperationLog o where o.userId = :userId order by o.useCount desc")
    List<OperationLog> findByUserIdOrderByUseCountDesc(@Param("userId") Long userId);
}
