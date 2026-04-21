package com.example.videoplatform.repository;

import com.example.videoplatform.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    // 原有的简单方法可以保留...
    List<Video> findByType(String type);
    List<Video> findByTitleContaining(String keyword);
    List<Video> findByCategoryContaining(String category);
    List<Video> findByIdNotIn(List<Long> ids);

    // 精确匹配标题（用于去重检查）
    Video findByTitle(String title);

    // --- 新增：万能组合查询（分页版本） ---
    @Query("SELECT v FROM Video v WHERE " +
            "(:keyword IS NULL OR v.title LIKE %:keyword%) AND " +
            "(:type IS NULL OR v.type = :type) AND " +
            "(:category IS NULL OR v.category LIKE %:category%) AND " +
            "(:region IS NULL OR v.region = :region) AND " +
            "(:year IS NULL OR v.year = :year) AND " +
            "(:language IS NULL OR v.language = :language)")
    Page<Video> searchVideos(String keyword, String type, String category,
                             String region, Integer year, String language, Pageable pageable);

    // --- 保留原有的非分页版本用于其他地方 ---
    @Query("SELECT v FROM Video v WHERE " +
            "(:keyword IS NULL OR v.title LIKE %:keyword%) AND " +
            "(:type IS NULL OR v.type = :type) AND " +
            "(:category IS NULL OR v.category LIKE %:category%) AND " +
            "(:region IS NULL OR v.region = :region) AND " +
            "(:year IS NULL OR v.year = :year) AND " +
            "(:language IS NULL OR v.language = :language)")
    List<Video> searchVideosList(String keyword, String type, String category,
                                 String region, Integer year, String language, Sort sort);
}