package com.cdek.timetracker.mapper;

import com.cdek.timetracker.model.AppUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Insert("""
            INSERT INTO users(username, password_hash, role)
            VALUES (#{username}, #{passwordHash}, #{role})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AppUser user);

    @Select("""
            SELECT id, username, password_hash, role, created_at
            FROM users
            WHERE username = #{username}
            """)
    AppUser findByUsername(String username);

    @Select("""
            SELECT id, username, password_hash, role, created_at
            FROM users
            WHERE id = #{id}
            """)
    AppUser findById(Long id);

    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE username = #{username})")
    boolean existsByUsername(String username);
}
