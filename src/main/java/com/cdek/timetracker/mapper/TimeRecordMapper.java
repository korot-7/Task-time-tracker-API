package com.cdek.timetracker.mapper;

import com.cdek.timetracker.model.TimeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TimeRecordMapper {

    @Insert("""
            INSERT INTO time_records(employee_id, task_id, started_at, ended_at, work_description, created_at)
            VALUES (#{employeeId}, #{taskId}, #{startedAt}, #{endedAt}, #{workDescription}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TimeRecord timeRecord);

    @Select("""
            SELECT id, employee_id, task_id, started_at, ended_at, work_description, created_at
            FROM time_records
            WHERE employee_id = #{employeeId}
              AND started_at >= #{from}
              AND ended_at <= #{to}
            ORDER BY started_at ASC
            """)
    List<TimeRecord> findByEmployeeAndPeriod(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
