package com.example.videoplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type; // MOVIE, SERIES

    // 推荐算法将直接匹配这个字段
    @Column(length = 500)
    private String category;

    private Integer playSourceType; // 0: 本地视频/直链 (使用DPlayer), 1: 外部网页链接 (使用Iframe)

    @Column(length = 1000)
    private String playUrl; // 播放地址 (本地相对路径或外部URL)
    private String description;
    private String coverUrl;
    private String localCoverPath; // 本地封面路径
    private Integer year;
    private String region;
    private String language;
    private Double score;
    // 存储JSON字符串，例如：[{"title":"第1集", "playSourceType":0, "playUrl":"..."}, {"title":"第2集"...}]
    @Column(columnDefinition = "TEXT")
    private String episodes;

    // 显式添加getter/setter方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPlaySourceType() { return playSourceType; }
    public void setPlaySourceType(Integer playSourceType) { this.playSourceType = playSourceType; }

    public String getPlayUrl() { return playUrl; }
    public void setPlayUrl(String playUrl) { this.playUrl = playUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getLocalCoverPath() { return localCoverPath; }
    public void setLocalCoverPath(String localCoverPath) { this.localCoverPath = localCoverPath; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getEpisodes() { return episodes; }
    public void setEpisodes(String episodes) { this.episodes = episodes; }
}