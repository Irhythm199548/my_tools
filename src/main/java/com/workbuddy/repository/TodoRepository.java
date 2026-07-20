package com.workbuddy.repository;

import com.workbuddy.entity.Priority;
import com.workbuddy.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUserIdAndCompleted(Long userId, Boolean completed);

    List<Todo> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Todo> findByUserIdAndPriority(Long userId, Priority priority);

    List<Todo> findByUserIdAndCompletedIsFalseOrderByPriorityAscCreatedAtDesc(Long userId);

    /** 统计某用户各优先级数量 */
    @Query("select t.priority, count(t) from Todo t where t.userId = :userId group by t.priority")
    List<Object[]> countByPriority(@Param("userId") Long userId);

    /** 某用户未完成任务数 */
    @Query("select count(t) from Todo t where t.userId = :userId and t.completed = false")
    long countUncompleted(@Param("userId") Long userId);
}
