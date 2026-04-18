package com.cdek.timetracker.mapper;

import com.cdek.timetracker.model.Task;
import com.cdek.timetracker.model.TaskStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface TaskMapper {

    @Insert("""
            INSERT INTO tasks(title, description, status, created_at, updated_at)
            VALUES (#{title}, #{description}, #{status}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Task task);

    @Select("SELECT id, title, description, status, created_at, updated_at FROM tasks WHERE id = #{id}")
    Task findById(Long id);

    @Select("SELECT EXISTS(SELECT 1 FROM tasks WHERE id = #{id})")
    boolean existsById(Long id);

    @Update("""
            UPDATE tasks
            SET status = #{status}, updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") TaskStatus status,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
