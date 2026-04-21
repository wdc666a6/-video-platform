package com.example.videoplatform.repository;

import com.example.videoplatform.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByCreateTimeDesc();

    List<Feedback> findByStatusOrderByCreateTimeDesc(String status);

    List<Feedback> findByUserIdOrderByCreateTimeDesc(Long userId);
}
