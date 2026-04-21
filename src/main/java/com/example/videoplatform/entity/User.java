package com.example.videoplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data // Lombok注解，自动生成Get/Set方法
@Entity // 告诉JPA这是一个要在数据库生成的表
@Table(name = "t_user") // 数据库表名叫 t_user
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // 用户名

    @Column(nullable = false)
    private String password; // 密码

    private String role; // 角色：admin (管理员) 或 user (普通用户)

    // 个性化标签：存储用户喜欢的类型，如 "科幻,动作"，用于推荐算法
    private String preferences;

    // 积分：用户通过评论获得积分，可用来请求新增影片
    private Integer points; // 积分，默认为0

    // 显式添加getter/setter方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}