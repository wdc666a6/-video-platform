package com.example.videoplatform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 请求用户ID

    @Column(length = 200)
    private String username; // 用户名（冗余存储，方便查询）

    @Column(length = 500)
    private String videoTitle; // 请求的影片名称

    @Column(length = 1000)
    private String description; // 描述信息（可选）

    // 请求状态: PENDING(待处理), APPROVED(已采纳), REJECTED(已拒绝)
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

    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public java.util.Date getCreateTime() { return createTime; }
    public void setCreateTime(java.util.Date createTime) { this.createTime = createTime; }

    public java.util.Date getReplyTime() { return replyTime; }
    public void setReplyTime(java.util.Date replyTime) { this.replyTime = replyTime; }
}
