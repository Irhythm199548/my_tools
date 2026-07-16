package com.workbuddy.service;

import com.workbuddy.entity.Todo;
import com.workbuddy.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * 待办事项 Service
 */
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository repository;

    public List<Todo> list(Boolean completed) {
        if (completed == null) {
            return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return repository.findByCompleted(completed);
    }

    public Todo get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("待办不存在: " + id));
    }

    @Transactional
    public Todo create(Todo todo) {
        return repository.save(todo);
    }

    @Transactional
    public Todo update(Long id, Todo source) {
        Todo existing = get(id);
        existing.setTitle(source.getTitle());
        existing.setDescription(source.getDescription());
        existing.setPriority(source.getPriority());
        existing.setCompleted(source.getCompleted());
        existing.setDueDate(source.getDueDate());
        return repository.save(existing);
    }

    @Transactional
    public Todo toggle(Long id) {
        Todo existing = get(id);
        existing.setCompleted(!Boolean.TRUE.equals(existing.getCompleted()));
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public long countUncompleted() {
        return repository.countUncompleted();
    }
}
