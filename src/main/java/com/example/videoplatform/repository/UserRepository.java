package com.example.videoplatform.repository;

import com.example.videoplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 自动生成 SQL: select * from t_user where username = ?
    User findByUsername(String username);
}