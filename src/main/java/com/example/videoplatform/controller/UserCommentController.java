package com.example.videoplatform.controller;

import com.example.videoplatform.common.Result;
import com.example.videoplatform.entity.Comment;
import com.example.videoplatform.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class UserCommentController {

    @Autowired
    private CommentService commentService;

    // 提交评论
    @PostMapping("/submit")
    public Result<Comment> submitComment(@RequestBody java.util.Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String username = (String) params.get("username");
            Long videoId = Long.valueOf(params.get("videoId").toString());
            Double rating = params.get("rating") != null ? Double.valueOf(params.get("rating").toString()) : null;
            String content = (String) params.get("content");

            Comment comment = commentService.saveComment(userId, username, videoId, rating, content);
            return Result.success(comment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 修改评论
    @PostMapping("/update")
    public Result<Comment> updateComment(@RequestBody java.util.Map<String, Object> params) {
        try {
            Long commentId = Long.valueOf(params.get("commentId").toString());
            Long userId = Long.valueOf(params.get("userId").toString());
            Double rating = params.get("rating") != null ? Double.valueOf(params.get("rating").toString()) : null;
            String content = (String) params.get("content");

            Comment comment = commentService.updateComment(commentId, userId, rating, content);
            return Result.success(comment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 删除评论
    @PostMapping("/delete")
    public Result<String> deleteComment(@RequestBody java.util.Map<String, Object> params) {
        try {
            Long commentId = Long.valueOf(params.get("commentId").toString());
            Long userId = Long.valueOf(params.get("userId").toString());

            commentService.deleteComment(commentId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取视频的所有评论
    @GetMapping("/list")
    public Result<List<Comment>> getVideoComments(@RequestParam Long videoId) {
        return Result.success(commentService.getVideoComments(videoId));
    }

    // 获取用户对某视频的所有评论
    @GetMapping("/my")
    public Result<List<Comment>> getMyVideoComments(@RequestParam Long userId, @RequestParam Long videoId) {
        return Result.success(commentService.getUserVideoComments(userId, videoId));
    }
}
