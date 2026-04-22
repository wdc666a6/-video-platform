package com.example.videoplatform.service;

import com.example.videoplatform.entity.Interaction;
import com.example.videoplatform.repository.InteractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class InteractionService {

    @Autowired
    private InteractionRepository interactionRepository;

    // 获取当前用户对某视频的交互状态 (用于前端回显)
    public Interaction getInteraction(Long userId, Long videoId) {
        return interactionRepository.findByUserIdAndVideoId(userId, videoId);
    }

    // 切换"收藏/取消收藏"状态
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

    // 获取用户收藏的视频ID列表
    public List<Long> getCollectedVideoIds(Long userId) {
        List<Interaction> interactions = interactionRepository.findByUserIdAndIsCollectedTrue(userId);
        return interactions.stream()
                .map(Interaction::getVideoId)
                .collect(java.util.stream.Collectors.toList());
    }
}
