package com.example.videoplatform.service;

import com.example.videoplatform.entity.Request;
import com.example.videoplatform.entity.User;
import com.example.videoplatform.repository.RequestRepository;
import com.example.videoplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    // 提交请求（消耗3积分）
    public Request submitRequest(Long userId, String videoTitle, String description) {
        // 1. 检查用户积分是否足够
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getPoints() == null || user.getPoints() < 3) {
            throw new RuntimeException("积分不足，需要3积分才能提交请求");
        }

        // 2. 扣除积分
        user.setPoints(user.getPoints() - 3);
        userRepository.save(user);

        // 3. 创建请求
        Request request = new Request();
        request.setUserId(userId);
        request.setUsername(user.getUsername());
        request.setVideoTitle(videoTitle);
        request.setDescription(description);
        request.setStatus("PENDING");
        request.setCreateTime(new Date());

        return requestRepository.save(request);
    }

    // 获取用户的所有请求
    public List<Request> getUserRequests(Long userId) {
        return requestRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    // 获取所有待处理的请求
    public List<Request> getPendingRequests() {
        return requestRepository.findByStatusOrderByCreateTimeDesc("PENDING");
    }

    // 获取所有请求（管理员用）
    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    // 管理员回复请求
    public void replyRequest(Long requestId, String status, String reply) {
        Request request = requestRepository.findById(requestId).orElse(null);
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }

        request.setStatus(status);
        request.setAdminReply(reply);
        request.setReplyTime(new Date());

        // 如果拒绝，返还3积分给用户
        if ("REJECTED".equals(status)) {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user != null) {
                int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
                user.setPoints(currentPoints + 3);
                userRepository.save(user);
            }
        }

        requestRepository.save(request);
    }
}
