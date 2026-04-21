package com.example.videoplatform.controller;

import com.example.videoplatform.common.Result;
import com.example.videoplatform.entity.User;
import com.example.videoplatform.repository.UserRepository;
import com.example.videoplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Result<User> login(@RequestBody User user) {
        try {
            User loginUser = userService.login(user.getUsername(), user.getPassword());
            return Result.success(loginUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        try {
            User newUser = userService.register(user);
            return Result.success(newUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    // 修改用户信息 (用于更新喜好标签)
    @PostMapping("/update")
    public Result<User> update(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        User existUser = userService.login(user.getUsername(), user.getPassword());
        return userService.updatePreferences(user.getId(), user.getPreferences());
    }

    // 获取当前用户信息（用于刷新积分等）
    @GetMapping("/current")
    public Result<User> getCurrentUser(@RequestParam Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
}