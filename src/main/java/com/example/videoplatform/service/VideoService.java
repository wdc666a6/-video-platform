package com.example.videoplatform.service;

import com.example.videoplatform.entity.Video;
import com.example.videoplatform.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.videoplatform.entity.Interaction;
import com.example.videoplatform.entity.User;
import com.example.videoplatform.repository.InteractionRepository;
import com.example.videoplatform.repository.UserRepository;
import com.example.videoplatform.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import java.util.List;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;
    // ... 在类中注入 UserRepo 和 InteractionRepo ...
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PosterService posterService;

    @Autowired
    private InteractionRepository interactionRepository;

    // 1. 添加或更新影视
    public Video saveVideo(Video video) {
        // save方法：如果ID为空则是新增，如果有ID则是更新
        return videoRepository.save(video);
    }

    // 批量导入影视（自动去重 + 自动获取海报）
    public java.util.Map<String, Object> batchImport(java.util.List<Video> videos) {
        int added = 0;
        int updated = 0;
        int failed = 0;
        int posterFetched = 0;  // 统计获取到的海报数量
        int coverDownloaded = 0; // 统计下载到本地的封面数量
        java.util.List<String> errors = new java.util.ArrayList<>();

        for (Video video : videos) {
            try {
                if (video.getTitle() == null || video.getTitle().trim().isEmpty()) {
                    errors.add("跳过：标题为空的记录");
                    failed++;
                    continue;
                }

                // 标准化标题
                video.setTitle(video.getTitle().trim());

                // 自动获取海报（如果封面为空）
                if (video.getCoverUrl() == null || video.getCoverUrl().isEmpty()) {
                    String poster = posterService.fetchPosterFromDouban(
                        video.getTitle(),
                        "SERIES".equals(video.getType())
                    );
                    if (poster != null && !poster.isEmpty()) {
                        video.setCoverUrl(poster);
                        posterFetched++;
                    } else {
                        // 设置默认占位图
                        video.setCoverUrl("https://via.placeholder.com/300x450?text=No+Poster");
                    }
                    // 添加延迟避免请求过快
                    Thread.sleep(300);
                }

                // 下载封面到本地
                if (video.getCoverUrl() != null && !video.getCoverUrl().isEmpty() &&
                    !video.getCoverUrl().contains("placeholder.com")) {
                    String localPath = posterService.getCoverImageService().downloadCover(
                        video.getCoverUrl(), video.getId(), video.getTitle()
                    );
                    if (localPath != null) {
                        video.setLocalCoverPath(localPath);
                        coverDownloaded++;
                    }
                }

                // 根据标题检查是否已存在
                Video existing = videoRepository.findByTitle(video.getTitle());
                if (existing != null) {
                    // 已存在，更新
                    video.setId(existing.getId());
                    videoRepository.save(video);
                    updated++;
                } else {
                    // 不存在，新增
                    videoRepository.save(video);
                    added++;
                }
            } catch (Exception e) {
                errors.add("导入失败 [" + video.getTitle() + "]: " + e.getMessage());
                failed++;
            }
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", videos.size());
        result.put("added", added);
        result.put("updated", updated);
        result.put("failed", failed);
        result.put("posterFetched", posterFetched);
        result.put("coverDownloaded", coverDownloaded);
        result.put("errors", errors);
        return result;
    }

    // 2. 删除影视
    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }

    // 3. 获取所有影视（管理员列表用）
    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    // 4. 根据ID查单个详情
    public Video findById(Long id) {
        return videoRepository.findById(id).orElse(null);
    }


    // 5. 根据条件查询视频 (用户端核心功能)

    // 修改这个方法，增加 region, year, language, sortField 参数
    public List<Video> findVideos(String keyword, String type, String category,
                                  String region, Integer year, String language, String sortField) {

        // 处理排序：默认按上映日期降序
        Sort sort = Sort.by(Sort.Direction.DESC, "year");
        if ("score".equals(sortField)) {
            sort = Sort.by(Sort.Direction.DESC, "score"); // 按评分
        } else if ("id".equals(sortField)) {
            sort = Sort.by(Sort.Direction.DESC, "id"); // 按入库时间(ID)
        }

        // 调用 Repository 的万能查询
        return videoRepository.searchVideosList(
                (keyword == null || keyword.isEmpty()) ? null : keyword,
                (type == null || type.isEmpty()) ? null : type,
                (category == null || category.isEmpty()) ? null : category,
                (region == null || region.isEmpty()) ? null : region,
                year,
                (language == null || language.isEmpty()) ? null : language,
                sort
        );
    }

    // 5.1 分页查询视频
    public PageResult<Video> findVideosPage(String keyword, String type, String category,
                                            String region, Integer year, String language, String sortField,
                                            int pageNum, int pageSize) {

        // 处理排序：默认按上映日期降序
        Sort sort = Sort.by(Sort.Direction.DESC, "year");
        if ("score".equals(sortField)) {
            sort = Sort.by(Sort.Direction.DESC, "score"); // 按评分
        } else if ("id".equals(sortField)) {
            sort = Sort.by(Sort.Direction.DESC, "id"); // 按入库时间(ID)
        }

        // 创建分页对象 (页码从0开始)
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

        // 调用 Repository 的分页查询
        Page<Video> page = videoRepository.searchVideos(
                (keyword == null || keyword.isEmpty()) ? null : keyword,
                (type == null || type.isEmpty()) ? null : type,
                (category == null || category.isEmpty()) ? null : category,
                (region == null || region.isEmpty()) ? null : region,
                year,
                (language == null || language.isEmpty()) ? null : language,
                pageable
        );

        return PageResult.of(page.getContent(), page.getTotalElements(), pageNum, pageSize);
    }

    // ... getRecommendations 等其他方法保持不变 ...
    /**
     * 核心推荐算法
     * @param userId 当前用户ID
     * @return 推荐视频列表
     */
    public List<Video> getRecommendations(Long userId) {
        // 1. 获取用户信息
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return new ArrayList<>(); // 用户不存在就返回空

        // 2. 获取用户收藏的视频ID列表
        List<Interaction> history = interactionRepository.findByUserIdAndIsCollectedTrue(userId);
        List<Long> collectedVideoIds = history.stream()
                .map(Interaction::getVideoId)
                .collect(Collectors.toList());

        // 3.以此为条件查询候选视频 (排除已收藏)
        List<Video> candidates;
        if (collectedVideoIds.isEmpty()) {
            candidates = videoRepository.findAll();
        } else {
            candidates = videoRepository.findByIdNotIn(collectedVideoIds);
        }

        // 4. 算法核心：打分排序
        // 用户偏好标签数组 (例如 ["科幻", "悬疑"])
        String[] prefTags = user.getPreferences() != null ? user.getPreferences().split(",") : new String[]{};

        // 对候选视频进行打分排序
        candidates.sort((v1, v2) -> {
            double score1 = calculateScore(v1, prefTags);
            double score2 = calculateScore(v2, prefTags);
            // 降序排列 (分数高的在前)
            return Double.compare(score2, score1);
        });

        // 5. 取前8个返回 (模拟“换一批”可以在前端做，或者在这里随机截取，这里简单处理取前8)
        if (candidates.size() > 8) {
            return candidates.subList(0, 8);
        }
        return candidates;
    }

    // 辅助方法：计算单个视频的匹配分
    // ... 前面的代码不变 ...

    // 辅助方法：计算单个视频的匹配分
    // 辅助方法：计算单个视频的匹配分 (多维度加权算法)
    private double calculateScore(Video video, String[] userTags) {
        double score = 0.0;

        // 1. 基础分：评分越高，基础分越高 (权重 1.0)
        if (video.getScore() != null) {
            score += video.getScore();
        }

        // 2. 遍历用户的所有喜好标签，逐个维度匹配
        for (String tag : userTags) {
            if (tag == null || tag.trim().isEmpty()) continue;
            tag = tag.trim(); // 去除空格

            // --- 维度 A: 详细分类 (权重 10) ---
            // 如: "科幻", "悬疑"
            if (video.getCategory() != null && video.getCategory().contains(tag)) {
                score += 10.0;
            }

            // --- 维度 B: 地区 (权重 8) ---
            // 如: "大陆", "美国"
            if (video.getRegion() != null && video.getRegion().contains(tag)) {
                score += 8.0;
            }

            // --- 维度 C: 语言 (权重 5) ---
            // 如: "英语", "粤语"
            if (video.getLanguage() != null && video.getLanguage().contains(tag)) {
                score += 5.0;
            }

            // --- 维度 D: 影视类型 (权重 3) ---
            // 数据库存的是 MOVIE/SERIES，但用户标签可能是 "电影"/"剧集"
            if ("电影".equals(tag) && "MOVIE".equals(video.getType())) {
                score += 3.0;
            }
            if ("剧集".equals(tag) && "SERIES".equals(video.getType())) {
                score += 3.0;
            }
        }

        // 3. 随机因子：增加 0~5 分的随机波动，防止推荐列表万年不变
        score += Math.random() * 5;

        return score;
    }

    // 获取用户收藏的视频列表
    public List<Video> getCollectedVideos(Long userId) {
        List<Interaction> interactions = interactionRepository.findByUserIdAndIsCollectedTrue(userId);
        List<Long> videoIds = interactions.stream()
                .map(Interaction::getVideoId)
                .collect(Collectors.toList());

        if (videoIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据ID列表批量查询视频
        return videoRepository.findAllById(videoIds);
    }
}
