package com.workbuddy.repository;

import com.workbuddy.entity.Priority;
import com.workbuddy.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByCompleted(Boolean completed);

    List<Todo> findByPriority(Priority priority);

    List<Todo> findByCompletedIsFalseOrderByPriorityAscCreatedAtDesc();

    /** 统计各优先级数量 */
    @Query("select t.priority, count(t) from Todo t group by t.priority")
    List<Object[]> countByPriority();

    /** 未完成任务数 */
    @Query("select count(t) from Todo t where t.completed = false")
    long countUncompleted();
}
