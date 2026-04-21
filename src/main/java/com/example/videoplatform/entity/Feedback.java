package com.example.videoplatform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 反馈用户ID

    @Column(length = 100)
    private String username; // 用户名（冗余存储，方便查询）

    @Column(length = 200)
    private String title; // 反馈标题

    @Column(length = 2000)
    private String content; // 反馈内容

    @Column(length = 100)
    private String category; // 反馈分类: bug(问题反馈), suggestion(功能建议), other(其他)

    // 反馈状态: PENDING(待处理), PROCESSED(已处理), CLOSED(已关闭)
    private String status;

    @Column(length = 1000)
    private String adminReply; // 管理员回复

    @JsonProperty("createTime")
    private java.util.Date createTime; // 创建时间

    @JsonProperty("replyTime")
    private java.util.Date replyTime; // 回复时间

    // 显式添加getter/setter方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public java.util.Date getCreateTime() { return createTime; }
    public void setCreateTime(java.util.Date createTime) { this.createTime = createTime; }

    public java.util.Date getReplyTime() { return replyTime; }
    public void setReplyTime(java.util.Date replyTime) { this.replyTime = replyTime; }
}
