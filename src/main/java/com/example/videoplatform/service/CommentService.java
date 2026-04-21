package com.example.videoplatform.service;

import com.example.videoplatform.entity.Comment;
import com.example.videoplatform.entity.User;
import com.example.videoplatform.repository.CommentRepository;
import com.example.videoplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    // 提交评论/打分
    public Comment saveComment(Long userId, String username, Long videoId, Double rating, String content) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setVideoId(videoId);
        comment.setRating(rating);
        comment.setContent(content);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setPointsEarned(0);

        // 积分逻辑：每次评论加1分，每个视频最多加2分
        if (content != null && !content.isEmpty()) {
            // 查询该用户对该视频已获得的积分
            Integer totalPoints = commentRepository.sumPointsEarnedByUserIdAndVideoId(userId, videoId);
            int currentPoints = (totalPoints != null ? totalPoints : 0);

            if (currentPoints < 2) {
                // 可以获得积分
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    int userPoints = user.getPoints() != null ? user.getPoints() : 0;
                    user.setPoints(userPoints + 1);
                    userRepository.save(user);
                    comment.setPointsEarned(1);
                }
            }
        }

        return commentRepository.save(comment);
    }

    // 修改评论
    public Comment updateComment(Long commentId, Long userId, Double rating, String content) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此评论");
        }

        if (rating != null) comment.setRating(rating);
        if (content != null) comment.setContent(content);
        comment.setUpdateTime(new Date());

        return commentRepository.save(comment);
    }

    // 删除评论
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }
        commentRepository.deleteById(commentId);
    }

    // 管理员删除评论
    public void deleteCommentByAdmin(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    // 获取视频的所有评论
    public List<Comment> getVideoComments(Long videoId) {
        if (videoId == null) {
            // 返回所有评论（管理员用）
            return commentRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createTime"));
        }
        return commentRepository.findByVideoIdOrderByCreateTimeDesc(videoId);
    }

    // 获取用户对某视频的所有评论
    public List<Comment> getUserVideoComments(Long userId, Long videoId) {
        return commentRepository.findByUserIdAndVideoIdOrderByCreateTimeDesc(userId, videoId);
    }

    // 获取某条评论
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElse(null);
    }

    // 获取所有评论（管理员用）
    public List<Comment> getAllCommentsForAdmin() {
        return commentRepository.findAll(
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createTime")
        );
    }
}
