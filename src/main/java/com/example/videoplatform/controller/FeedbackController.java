package com.example.videoplatform.controller;

import com.example.videoplatform.entity.Feedback;
import com.example.videoplatform.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    // 用户提交反馈
    @PostMapping("/submit")
    public Map<String, Object> submitFeedback(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = Long.parseLong(params.get("userId").toString());
            String username = params.get("username").toString();
            String title = params.get("title").toString();
            String content = params.get("content").toString();
            String category = params.get("category") != null ? params.get("category").toString() : "other";

            if (title == null || title.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "请输入反馈标题");
                return result;
            }

            if (content == null || content.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "请输入反馈内容");
                return result;
            }

            Feedback feedback = new Feedback();
            feedback.setUserId(userId);
            feedback.setUsername(username);
            feedback.setTitle(title.trim());
            feedback.setContent(content.trim());
            feedback.setCategory(category);
            feedback.setStatus("PENDING");
            feedback.setCreateTime(new Date());

            feedbackRepository.save(feedback);

            result.put("code", 200);
            result.put("msg", "反馈提交成功");
            result.put("data", feedback);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "提交失败: " + e.getMessage());
        }

        return result;
    }

    // 获取当前用户的反馈列表
    @GetMapping("/my")
    public Map<String, Object> getMyFeedback(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Feedback> list = feedbackRepository.findByUserIdOrderByCreateTimeDesc(userId);
            result.put("code", 200);
            result.put("data", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取失败: " + e.getMessage());
        }
        return result;
    }
}
