package com.workbuddy.repository;

import com.workbuddy.entity.Pomodoro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface PomodoroRepository extends JpaRepository<Pomodoro, Long> {

    List<Pomodoro> findTop50ByOrderByStartTimeDesc();

    /** 累计专注秒数 */
    @Query("select coalesce(sum(p.duration),0) from Pomodoro p")
    long sumDuration();

    /** 今日专注秒数（按传入的当日起止范围，避免依赖 MySQL 服务器时区） */
    @Query("select coalesce(sum(p.duration),0) from Pomodoro p where p.startTime >= :start and p.startTime < :end")
    long sumDurationToday(@Param("start") Date start, @Param("end") Date end);

    /** 累计番茄次数 */
    @Query("select count(p.id) from Pomodoro p")
    long countAll();

    /** 今日番茄次数（按传入的当日起止范围） */
    @Query("select count(p.id) from Pomodoro p where p.startTime >= :start and p.startTime < :end")
    long countToday(@Param("start") Date start, @Param("end") Date end);
}
