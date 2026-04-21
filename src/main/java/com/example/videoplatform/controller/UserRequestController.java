package com.example.videoplatform.controller;

import com.example.videoplatform.common.Result;
import com.example.videoplatform.entity.Request;
import com.example.videoplatform.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/request")
public class UserRequestController {

    @Autowired
    private RequestService requestService;

    // 提交请求（消耗3积分）
    @PostMapping("/submit")
    public Result<Request> submitRequest(@RequestBody java.util.Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String videoTitle = (String) params.get("videoTitle");
            String description = (String) params.get("description");

            if (videoTitle == null || videoTitle.isEmpty()) {
                return Result.error("请输入影片名称");
            }

            Request request = requestService.submitRequest(userId, videoTitle, description);
            return Result.success(request);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取我的请求列表
    @GetMapping("/my")
    public Result<List<Request>> getMyRequests(@RequestParam Long userId) {
        return Result.success(requestService.getUserRequests(userId));
    }
}
