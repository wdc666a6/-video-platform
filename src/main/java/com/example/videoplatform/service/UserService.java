package com.example.videoplatform.service;

import com.example.videoplatform.entity.User;
import com.example.videoplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 登录逻辑
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    // 注册逻辑
    public User register(User user) {
        // 检查用户名是否重复
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 默认设置为普通用户
        user.setRole("user");
        return userRepository.save(user);
    }
    // 更新用户偏好
    public com.example.videoplatform.common.Result<User> updatePreferences(Long userId, String preferences) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setPreferences(preferences);
            userRepository.save(user);
            return com.example.videoplatform.common.Result.success(user);
        }
        return com.example.videoplatform.common.Result.error("用户不存在");
    }
    // 1. 获取所有用户列表
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    // 2. 删除用户
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}