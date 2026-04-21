package com.example.videoplatform.repository;

import com.example.videoplatform.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 查询某视频的所有评论
    List<Comment> findByVideoIdOrderByCreateTimeDesc(Long videoId);

    // 查询某用户对某视频的所有评论
    List<Comment> findByUserIdAndVideoIdOrderByCreateTimeDesc(Long userId, Long videoId);

    // 查询某用户对某视频获得的积分总和
    @Query("SELECT COALESCE(SUM(c.pointsEarned), 0) FROM Comment c WHERE c.userId = :userId AND c.videoId = :videoId")
    Integer sumPointsEarnedByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") Long videoId);
}
