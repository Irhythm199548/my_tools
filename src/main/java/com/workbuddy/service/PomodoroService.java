package com.workbuddy.service;

import com.workbuddy.entity.Pomodoro;
import com.workbuddy.repository.PomodoroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 番茄钟 Service
 */
@Service
@RequiredArgsConstructor
public class PomodoroService {

    /** 统一用时区，避免依赖 JVM / MySQL 服务器默认时区导致「今日」统计错位 */
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final PomodoroRepository repository;

    public List<Pomodoro> recent() {
        return repository.findTop50ByOrderByStartTimeDesc();
    }

    public Pomodoro save(Pomodoro pomodoro) {
        return repository.save(pomodoro);
    }

    public long totalSeconds() {
        return repository.sumDuration();
    }

    public long todaySeconds() {
        Date[] range = todayRange();
        return repository.sumDurationToday(range[0], range[1]);
    }

    public long totalCount() {
        return repository.countAll();
    }

    public long todayCount() {
        Date[] range = todayRange();
        return repository.countToday(range[0], range[1]);
    }

    /** 返回 [今日 00:00, 明日 00:00) 的 Date 区间（Asia/Shanghai） */
    private Date[] todayRange() {
        LocalDate today = LocalDate.now(ZONE);
        Date start = Date.from(today.atStartOfDay(ZONE).toInstant());
        Date end = Date.from(today.plusDays(1).atStartOfDay(ZONE).toInstant());
        return new Date[]{start, end};
    }
}
