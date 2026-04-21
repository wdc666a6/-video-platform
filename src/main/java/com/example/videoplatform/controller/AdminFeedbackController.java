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
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    // 获取所有反馈
    @GetMapping("/list")
    public Map<String, Object> getAllFeedback() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Feedback> list = feedbackRepository.findAllByOrderByCreateTimeDesc();
            result.put("code", 200);
            result.put("data", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取失败: " + e.getMessage());
        }
        return result;
    }

    // 按状态获取反馈
    @GetMapping("/filter")
    public Map<String, Object> getFeedbackByStatus(@RequestParam String status) {
        Map<String, Object> result = new HashMap<>();
        try {
            if ("all".equals(status)) {
                List<Feedback> list = feedbackRepository.findAllByOrderByCreateTimeDesc();
                result.put("code", 200);
                result.put("data", list);
            } else {
                List<Feedback> list = feedbackRepository.findByStatusOrderByCreateTimeDesc(status);
                result.put("code", 200);
                result.put("data", list);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取失败: " + e.getMessage());
        }
        return result;
    }

    // 管理员回复反馈
    @PostMapping("/reply")
    public Map<String, Object> replyFeedback(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long feedbackId = Long.parseLong(params.get("feedbackId").toString());
            String status = params.get("status").toString();
            String reply = params.get("reply").toString();

            Feedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
            if (feedback == null) {
                result.put("code", 404);
                result.put("msg", "反馈不存在");
                return result;
            }

            feedback.setStatus(status);
            feedback.setAdminReply(reply);
            feedback.setReplyTime(new Date());
            feedbackRepository.save(feedback);

            result.put("code", 200);
            result.put("msg", "回复成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "回复失败: " + e.getMessage());
        }

        return result;
    }

    // 删除反馈
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteFeedback(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            feedbackRepository.deleteById(id);
            result.put("code", 200);
            result.put("msg", "删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除失败: " + e.getMessage());
        }
        return result;
    }
}
