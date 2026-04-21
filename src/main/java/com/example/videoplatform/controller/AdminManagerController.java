package com.example.videoplatform.controller;

import com.example.videoplatform.common.Result;
import com.example.videoplatform.entity.Comment;
import com.example.videoplatform.entity.Interaction;
import com.example.videoplatform.entity.Request;
import com.example.videoplatform.entity.User;
import com.example.videoplatform.service.CommentService;
import com.example.videoplatform.service.InteractionService;
import com.example.videoplatform.service.RequestService;
import com.example.videoplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminManagerController {

    @Autowired
    private UserService userService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private CommentService commentService;

    // --- 用户管理接口 ---

    @GetMapping("/user/list")
    public Result<List<User>> listUsers() {
        return Result.success(userService.findAll());
    }

    @DeleteMapping("/user/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户删除成功");
    }

    // --- 评论管理接口 ---

    @GetMapping("/comment/list")
    public Result<List<Comment>> listComments() {
        return Result.success(commentService.getAllCommentsForAdmin());
    }

    @DeleteMapping("/comment/delete/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        commentService.deleteCommentByAdmin(id);
        return Result.success("评论删除成功");
    }

    // --- 影片请求管理接口 ---

    // 获取所有请求
    @GetMapping("/request/list")
    public Result<List<Request>> listRequests() {
        return Result.success(requestService.getAllRequests());
    }

    // 获取待处理的请求
    @GetMapping("/request/pending")
    public Result<List<Request>> getPendingRequests() {
        return Result.success(requestService.getPendingRequests());
    }

    // 回复请求
    @PostMapping("/request/reply")
    public Result<String> replyRequest(@RequestBody java.util.Map<String, Object> params) {
        try {
            Long requestId = Long.valueOf(params.get("requestId").toString());
            String status = (String) params.get("status"); // APPROVED 或 REJECTED
            String reply = (String) params.get("reply");

            requestService.replyRequest(requestId, status, reply);
            return Result.success("回复成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}