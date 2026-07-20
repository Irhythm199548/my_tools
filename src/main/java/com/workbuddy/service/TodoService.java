package com.workbuddy.service;

import com.workbuddy.common.BizException;
import com.workbuddy.entity.Todo;
import com.workbuddy.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * 待办事项 Service（按用户隔离）
 */
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository repository;

    public List<Todo> list(Long userId, Boolean completed) {
        if (completed == null) {
            return repository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return repository.findByUserIdAndCompleted(userId, completed);
    }

    public Todo get(Long userId, Long id) {
        Todo t = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("待办不存在: " + id));
        if (!userId.equals(t.getUserId())) {
            throw new BizException("无权访问该待办");
        }
        return t;
    }

    @Transactional
    public Todo create(Long userId, Todo todo) {
        todo.setId(null);
        todo.setUserId(userId);
        return repository.save(todo);
    }

    @Transactional
    public Todo update(Long userId, Long id, Todo source) {
        Todo existing = get(userId, id);
        existing.setTitle(source.getTitle());
        existing.setDescription(source.getDescription());
        existing.setPriority(source.getPriority());
        existing.setCompleted(source.getCompleted());
        existing.setDueDate(source.getDueDate());
        return repository.save(existing);
    }

    @Transactional
    public Todo toggle(Long userId, Long id) {
        Todo existing = get(userId, id);
        existing.setCompleted(!Boolean.TRUE.equals(existing.getCompleted()));
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Todo existing = get(userId, id);
        repository.delete(existing);
    }

    public long countUncompleted(Long userId) {
        return repository.countUncompleted(userId);
    }
}
