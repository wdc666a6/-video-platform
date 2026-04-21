package com.example.videoplatform.service;

import com.example.videoplatform.entity.Interaction;
import com.example.videoplatform.entity.User;
import com.example.videoplatform.repository.InteractionRepository;
import com.example.videoplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class InteractionService {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. 获取当前用户对某视频的交互状态 (用于前端回显)
    public Interaction getInteraction(Long userId, Long videoId) {
        return interactionRepository.findByUserIdAndVideoId(userId, videoId);
    }

    // 2. 独立功能：切换”收藏/取消收藏”状态
    public boolean toggleCollected(Long userId, Long videoId) {
        Interaction interaction = interactionRepository.findByUserIdAndVideoId(userId, videoId);
        if (interaction == null) {
            interaction = new Interaction();
            interaction.setUserId(userId);
            interaction.setVideoId(videoId);
            interaction.setCreateTime(new Date());
            interaction.setCollected(true); // 默认为收藏
        } else {
            // 取反：如果原来是true变false，false变true
            interaction.setCollected(!interaction.isCollected());
        }
        interactionRepository.save(interaction);
        return interaction.isCollected();
    }

    // 3. 提交评论/打分 (注意：不再强制设为收藏)
    public void saveComment(Long userId, Long videoId, Double rating, String comment) {
        Interaction interaction = interactionRepository.findByUserIdAndVideoId(userId, videoId);
        if (interaction == null) {
            interaction = new Interaction();
            interaction.setUserId(userId);
            interaction.setVideoId(videoId);
            interaction.setCreateTime(new Date());
            interaction.setCollected(false); // 仅仅评论不代表收藏，除非用户点收藏
            interaction.setPointsEarned(0); // 初始化积分为0
        }
        if (rating != null) interaction.setRating(rating);
        if (comment != null) interaction.setComment(comment);

        // 积分逻辑：每次评论加1分，每个视频最多加2分
        if (comment != null && !comment.isEmpty()) {
            int currentPoints = interaction.getPointsEarned() != null ? interaction.getPointsEarned() : 0;
            if (currentPoints < 2) {
                // 可以获得积分
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    int userPoints = user.getPoints() != null ? user.getPoints() : 0;
                    user.setPoints(userPoints + 1);
                    userRepository.save(user);
                    interaction.setPointsEarned(currentPoints + 1);
                }
            }
        }

        interactionRepository.save(interaction);
    }

    // 4. 用户删除自己的评论
    public void deleteMyComment(Long userId, Long interactionId) {
        Interaction interaction = interactionRepository.findById(interactionId).orElse(null);
        if (interaction != null && interaction.getUserId().equals(userId)) {
            // 我们不删除整行记录，因为可能还包含“已看”状态
            // 我们只把评论内容和打分置空
            interaction.setComment(null);
            interaction.setRating(null);
            interactionRepository.save(interaction);
        } else {
            throw new RuntimeException("无法删除：评论不存在或无权操作");
        }
    }

    // ...findAll, deleteInteraction(管理员用的) 保持不变...
    public List<Interaction> findAll() {
        return interactionRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createTime"));
    }

    // 查某视频的评论列表 (只查有内容的)
    public List<Interaction> getComments(Long videoId) {
        // 这里简单过滤掉 comment 为空的记录，或者在 Repository 写 SQL 过滤
        // 为了省事，我们在 Java 层过滤
        List<Interaction> all = interactionRepository.findByVideoIdOrderByCreateTimeDesc(videoId);
        return all.stream().filter(i -> i.getComment() != null && !i.getComment().isEmpty()).toList();
    }

    // 管理员删除
    public void deleteInteraction(Long id) {
        interactionRepository.deleteById(id);
    }

    // 获取用户收藏的视频ID列表
    public List<Long> getCollectedVideoIds(Long userId) {
        List<Interaction> interactions = interactionRepository.findByUserIdAndIsCollectedTrue(userId);
        return interactions.stream()
                .map(Interaction::getVideoId)
                .collect(java.util.stream.Collectors.toList());
    }
}