package com.example.videoplatform.controller;

import com.example.videoplatform.common.Result;
import com.example.videoplatform.common.PageResult;
import com.example.videoplatform.entity.Video;
import com.example.videoplatform.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.videoplatform.service.InteractionService;
import com.example.videoplatform.entity.Interaction;

import java.util.List;

@RestController
@RequestMapping("/api/video") // 用户端接口前缀
public class UserVideoController {

    @Autowired
    private VideoService videoService;
    @Autowired
    private InteractionService interactionService;

    // 获取视频列表 (支持 搜索、按类型、按分类)
    // 访问示例: /api/video/list?type=MOVIE 或 /api/video/list?keyword=流浪

    @GetMapping("/list")
    public Result<List<Video>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,   // 新增
            @RequestParam(required = false) Integer year,    // 新增
            @RequestParam(required = false) String language, // 新增
            @RequestParam(required = false, defaultValue = "time") String sort // 新增排序
    ) {
        List<Video> list = videoService.findVideos(keyword, type, category, region, year, language, sort);
        return Result.success(list);
    }

    // 分页查询视频列表
    // 访问示例: /api/video/list-page?page=1&pageSize=30&type=MOVIE
    @GetMapping("/list-page")
    public Result<PageResult<Video>> listPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String language,
            @RequestParam(required = false, defaultValue = "time") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int pageSize
    ) {
        PageResult<Video> pageResult = videoService.findVideosPage(keyword, type, category, region, year, language, sort, page, pageSize);
        return Result.success(pageResult);
    }

    // 推荐接口
    // GET /api/video/recommend?userId=1
    @GetMapping("/recommend")
    public Result<List<Video>> recommend(@RequestParam Long userId) {
        List<Video> list = videoService.getRecommendations(userId);
        return Result.success(list);
    }

    // 获取视频详情
    @GetMapping("/detail/{id}")
    public Result<Video> detail(@PathVariable Long id) {
        Video video = videoService.findById(id);
        return Result.success(video);
    }

    // 获取我的交互状态 (用于详情页判断我是否已收藏)
    @GetMapping("/interaction")
    public Result<Interaction> getInteraction(@RequestParam Long userId, @RequestParam Long videoId) {
        return Result.success(interactionService.getInteraction(userId, videoId));
    }

    // 切换收藏状态
    @PostMapping("/toggle-collect")
    public Result<Boolean> toggleCollected(@RequestBody java.util.Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long videoId = Long.valueOf(params.get("videoId").toString());
        boolean isCollected = interactionService.toggleCollected(userId, videoId);
        return Result.success(isCollected);
    }

    // 获取用户的收藏列表
    @GetMapping("/collected")
    public Result<List<Video>> getCollectedList(@RequestParam Long userId) {
        List<Video> list = videoService.getCollectedVideos(userId);
        return Result.success(list);
    }
}
