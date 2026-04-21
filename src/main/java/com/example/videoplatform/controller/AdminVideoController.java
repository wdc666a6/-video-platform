package com.example.videoplatform.controller;

import com.example.videoplatform.common.Result;
import com.example.videoplatform.entity.Video;
import com.example.videoplatform.service.VideoService;
import com.example.videoplatform.service.PosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/video") // 统一前缀，访问路径为 /admin/video/xxx
public class AdminVideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private PosterService posterService;

    // 新增接口: POST /admin/video/add
    @PostMapping("/add")
    public Result<Video> add(@RequestBody Video video) {
        Video saved = videoService.saveVideo(video);
        return Result.success(saved);
    }

    // 修改接口: PUT /admin/video/update
    @PutMapping("/update")
    public Result<Video> update(@RequestBody Video video) {
        if (video.getId() == null) {
            return Result.error("修改必须携带ID");
        }
        Video saved = videoService.saveVideo(video);
        return Result.success(saved);
    }

    // 删除接口: DELETE /admin/video/delete/1
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return Result.success("删除成功");
    }

    // 列表接口: GET /admin/video/list
    @GetMapping("/list")
    public Result<List<Video>> list() {
        return Result.success(videoService.findAll());
    }

    // 批量导入接口: POST /admin/video/batch-import
    @PostMapping("/batch-import")
    public Result<java.util.Map<String, Object>> batchImport(@RequestBody java.util.List<Video> videos) {
        java.util.Map<String, Object> result = videoService.batchImport(videos);
        return Result.success(result);
    }

    // 获取单个视频的海报: POST /admin/video/fetch-poster/{id}
    @PostMapping("/fetch-poster/{id}")
    public Result<Map<String, Object>> fetchPoster(@PathVariable Long id) {
        Video video = videoService.findById(id);
        if (video == null) {
            return Result.error("视频不存在");
        }

        String oldCover = video.getCoverUrl();
        String newCover = posterService.fetchPosterFromDouban(
            video.getTitle(),
            "SERIES".equals(video.getType())
        );

        Map<String, Object> result = new java.util.HashMap<>();
        if (newCover != null && !newCover.isEmpty()) {
            video.setCoverUrl(newCover);

            // 下载封面到本地
            String localPath = posterService.getCoverImageService().downloadCover(
                newCover, video.getId(), video.getTitle()
            );
            if (localPath != null) {
                video.setLocalCoverPath(localPath);
                result.put("localCoverPath", localPath);
            }

            videoService.saveVideo(video);
            result.put("success", true);
            result.put("coverUrl", newCover);
            result.put("message", "获取成功");
            System.out.println("✓ 单个获取成功: " + video.getTitle() + " -> " + newCover);
        } else {
            result.put("success", false);
            result.put("message", "未找到匹配的海报，请检查影片名称是否正确");
            System.out.println("✗ 单个获取失败: " + video.getTitle());
        }
        result.put("oldCoverUrl", oldCover);
        return Result.success(result);
    }

    // 下载所有现有视频的封面到本地: POST /admin/video/download-all-covers
    @PostMapping("/download-all-covers")
    public Result<Map<String, Object>> downloadAllCovers() {
        List<Video> allVideos = videoService.findAll();

        int successCount = 0;
        int failCount = 0;
        int skippedCount = 0;
        java.util.List<String> failedTitles = new java.util.ArrayList<>();

        for (Video video : allVideos) {
            // 跳过已有本地封面的视频
            if (video.getLocalCoverPath() != null && !video.getLocalCoverPath().isEmpty()) {
                skippedCount++;
                continue;
            }

            // 跳过没有外部封面的视频
            if (video.getCoverUrl() == null || video.getCoverUrl().isEmpty() ||
                video.getCoverUrl().contains("placeholder.com")) {
                failedTitles.add(video.getTitle() + ": 无外部封面");
                failCount++;
                continue;
            }

            try {
                String localPath = posterService.getCoverImageService().downloadCover(
                    video.getCoverUrl(), video.getId(), video.getTitle()
                );

                if (localPath != null) {
                    video.setLocalCoverPath(localPath);
                    videoService.saveVideo(video);
                    successCount++;
                    System.out.println("✓ 下载成功: " + video.getTitle() + " -> " + localPath);
                } else {
                    failCount++;
                    failedTitles.add(video.getTitle() + ": 下载失败");
                    System.out.println("✗ 下载失败: " + video.getTitle());
                }

                // 延迟避免请求过快
                Thread.sleep(200);
            } catch (Exception e) {
                failCount++;
                failedTitles.add(video.getTitle() + ": " + e.getMessage());
                System.err.println("✗ 下载异常: " + video.getTitle() + " - " + e.getMessage());
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", allVideos.size());
        result.put("skipped", skippedCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failedTitles", failedTitles);
        result.put("message", "下载完成: 成功 " + successCount + " 个, 跳过 " + skippedCount + " 个, 失败 " + failCount + " 个");
        return Result.success(result);
    }

    // 批量获取所有缺失海报的视频: POST /admin/video/fetch-missing-posters
    @PostMapping("/fetch-missing-posters")
    public Result<Map<String, Object>> fetchMissingPosters() {
        List<Video> allVideos = videoService.findAll();

        int successCount = 0;
        int failCount = 0;
        int skippedCount = 0;  // 新增：跳过计数
        java.util.List<String> failedTitles = new java.util.ArrayList<>();

        for (Video video : allVideos) {
            // 只处理封面为空或者是默认占位图的视频
            boolean needsFetch = video.getCoverUrl() == null ||
                video.getCoverUrl().isEmpty() ||
                video.getCoverUrl().contains("placeholder.com") ||
                video.getCoverUrl().contains("example.com/poster");

            if (!needsFetch) {
                skippedCount++;
                continue;
            }

            try {
                String poster = posterService.fetchPosterFromDouban(
                    video.getTitle(),
                    "SERIES".equals(video.getType())
                );

                if (poster != null && !poster.isEmpty()) {
                    video.setCoverUrl(poster);

                    // 下载封面到本地
                    String localPath = posterService.getCoverImageService().downloadCover(
                        poster, video.getId(), video.getTitle()
                    );
                    if (localPath != null) {
                        video.setLocalCoverPath(localPath);
                    }

                    videoService.saveVideo(video);
                    successCount++;
                    System.out.println("✓ 获取成功: " + video.getTitle() + " -> " + poster);
                } else {
                    failCount++;
                    failedTitles.add(video.getTitle());
                    System.out.println("✗ 获取失败: " + video.getTitle());
                }

                // 延迟避免请求过快
                Thread.sleep(500);
            } catch (Exception e) {
                failCount++;
                failedTitles.add(video.getTitle() + ": " + e.getMessage());
                System.err.println("✗ 获取异常: " + video.getTitle() + " - " + e.getMessage());
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", allVideos.size());
        result.put("skipped", skippedCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failedTitles", failedTitles);
        return Result.success(result);
    }

    // 批量获取所有视频的海报（强制刷新）: POST /admin/video/fetch-all-posters
    @PostMapping("/fetch-all-posters")
    public Result<Map<String, Object>> fetchAllPosters() {
        List<Video> allVideos = videoService.findAll();

        int successCount = 0;
        int failCount = 0;
        java.util.List<String> failedTitles = new java.util.ArrayList<>();

        for (Video video : allVideos) {
            try {
                String poster = posterService.fetchPosterFromDouban(
                    video.getTitle(),
                    "SERIES".equals(video.getType())
                );

                if (poster != null && !poster.isEmpty()) {
                    video.setCoverUrl(poster);

                    // 下载封面到本地
                    String localPath = posterService.getCoverImageService().downloadCover(
                        poster, video.getId(), video.getTitle()
                    );
                    if (localPath != null) {
                        video.setLocalCoverPath(localPath);
                    }

                    videoService.saveVideo(video);
                    successCount++;
                    System.out.println("✓ 强制刷新成功: " + video.getTitle());
                } else {
                    failCount++;
                    failedTitles.add(video.getTitle());
                    System.out.println("✗ 强制刷新失败: " + video.getTitle());
                }

                // 延迟避免请求过快
                Thread.sleep(500);
            } catch (Exception e) {
                failCount++;
                failedTitles.add(video.getTitle() + ": " + e.getMessage());
                System.err.println("✗ 强制刷新异常: " + video.getTitle() + " - " + e.getMessage());
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", allVideos.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failedTitles", failedTitles);
        return Result.success(result);
    }

    // 清除所有MyAnimeList封面（Jikan API错误匹配导致）
    @PostMapping("/clear-myanimelist-posters")
    public Result<Map<String, Object>> clearMyAnimeListPosters() {
        List<Video> allVideos = videoService.findAll();

        int clearedCount = 0;
        int keptCount = 0;

        for (Video video : allVideos) {
            if (video.getCoverUrl() != null && video.getCoverUrl().contains("myanimelist.net")) {
                video.setCoverUrl(null);
                videoService.saveVideo(video);
                clearedCount++;
                System.out.println("✓ 清除错误封面: " + video.getTitle());
            } else {
                keptCount++;
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", allVideos.size());
        result.put("clearedCount", clearedCount);
        result.put("keptCount", keptCount);
        result.put("message", "已清除 " + clearedCount + " 个错误的MyAnimeList封面");
        return Result.success(result);
    }
}