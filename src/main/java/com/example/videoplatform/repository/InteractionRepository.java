package com.example.videoplatform.repository;

import com.example.videoplatform.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    // 查找某人对某视频的互动记录
    Interaction findByUserIdAndVideoId(Long userId, Long videoId);

    // 查找某人收藏的所有视频记录
    List<Interaction> findByUserIdAndIsCollectedTrue(Long userId);
    // 查某个视频的所有评论 (按时间倒序)
    // SQL: select * from t_interaction where video_id = ? order by create_time desc
    List<Interaction> findByVideoIdOrderByCreateTimeDesc(Long videoId);
}