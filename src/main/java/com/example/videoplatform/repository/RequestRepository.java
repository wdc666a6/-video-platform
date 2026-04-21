package com.example.videoplatform.repository;

import com.example.videoplatform.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // 查询某用户的所有请求
    List<Request> findByUserIdOrderByCreateTimeDesc(Long userId);

    // 查询待处理的请求
    List<Request> findByStatusOrderByCreateTimeDesc(String status);
}
