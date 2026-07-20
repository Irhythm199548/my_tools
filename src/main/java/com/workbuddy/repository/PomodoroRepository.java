package com.workbuddy.repository;

import com.workbuddy.entity.Pomodoro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface PomodoroRepository extends JpaRepository<Pomodoro, Long> {

    List<Pomodoro> findByUserIdOrderByStartTimeDesc(Long userId);

    /** 累计专注秒数 */
    @Query("select coalesce(sum(p.duration),0) from Pomodoro p where p.userId = :userId")
    long sumDuration(@Param("userId") Long userId);

    /** 今日专注秒数（按传入的当日起止范围，避免依赖 MySQL 服务器时区） */
    @Query("select coalesce(sum(p.duration),0) from Pomodoro p where p.userId = :userId and p.startTime >= :start and p.startTime < :end")
    long sumDurationToday(@Param("userId") Long userId, @Param("start") Date start, @Param("end") Date end);

    /** 累计番茄次数 */
    @Query("select count(p.id) from Pomodoro p where p.userId = :userId")
    long countAll(@Param("userId") Long userId);

    /** 今日番茄次数（按传入的当日起止范围） */
    @Query("select count(p.id) from Pomodoro p where p.userId = :userId and p.startTime >= :start and p.startTime < :end")
    long countToday(@Param("userId") Long userId, @Param("start") Date start, @Param("end") Date end);
}
