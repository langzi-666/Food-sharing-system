package com.foodsharing.service;

import com.foodsharing.entity.Post;
import com.foodsharing.entity.User;
import com.foodsharing.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;

    public RecommendationService(PostRepository postRepository,
                                 LikeRepository likeRepository,
                                 FavoriteRepository favoriteRepository,
                                 FollowRepository followRepository,
                                 CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.followRepository = followRepository;
    }

    /**
     * 个性化推荐：基于用户行为
     */
    public List<Post> getPersonalizedRecommendations(User user, int limit) {
        if (user == null) {
            return getPopularPosts(limit);
        }

        // 1. 获取用户点赞、收藏、评论过的内容
        Set<Long> likedPostIds = likeRepository.findByUser(user).stream()
                .map(l -> l.getPost().getId())
                .collect(Collectors.toSet());
        
        Set<Long> favoritedPostIds = favoriteRepository.findByUser(user).stream()
                .map(f -> f.getPost().getId())
                .collect(Collectors.toSet());
        
        Set<Long> commentedPostIds = commentRepository.findByUser(user).stream()
                .map(c -> c.getPost().getId())
                .collect(Collectors.toSet());

        Set<Long> interactedPostIds = new HashSet<>();
        interactedPostIds.addAll(likedPostIds);
        interactedPostIds.addAll(favoritedPostIds);
        interactedPostIds.addAll(commentedPostIds);

        // 2. 获取用户关注的人发布的内容
        Set<Long> followingUserIds = followRepository.findByFollower(user).stream()
                .map(f -> f.getFollowing().getId())
                .collect(Collectors.toSet());

        // 3. 基于分类和标签的推荐
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> tagIds = new HashSet<>();
        
        for (Long postId : interactedPostIds) {
            postRepository.findById(postId).ifPresent(post -> {
                if (post.getCategory() != null) {
                    categoryIds.add(post.getCategory().getId());
                }
                tagIds.addAll(post.getTags().stream()
                        .map(t -> t.getId())
                        .collect(Collectors.toSet()));
            });
        }

        // 4. 获取推荐内容（排除已交互的内容）
        List<Post> recommendations = new ArrayList<>();
        
        // 优先推荐关注用户的内容
        if (!followingUserIds.isEmpty()) {
            List<Post> followingPosts = postRepository.findByAuthorIdIn(
                    followingUserIds, PageRequest.of(0, limit * 2)
            ).getContent();
            recommendations.addAll(followingPosts.stream()
                    .filter(p -> !interactedPostIds.contains(p.getId()))
                    .limit(Math.max(1, limit / 2))
                    .collect(Collectors.toList()));
        }

        // 基于分类推荐
        if (!categoryIds.isEmpty() && recommendations.size() < limit) {
            List<Post> categoryPosts = postRepository.findAll().stream()
                    .filter(p -> p.getCategory() != null && categoryIds.contains(p.getCategory().getId()))
                    .filter(p -> !interactedPostIds.contains(p.getId()))
                    .sorted((a, b) -> Long.compare(b.getViewCount(), a.getViewCount()))
                    .limit(limit - recommendations.size())
                    .collect(Collectors.toList());
            recommendations.addAll(categoryPosts);
        }

        // 如果还不够，补充热门内容
        if (recommendations.size() < limit) {
            List<Post> popularPosts = getPopularPosts(limit * 2);
            popularPosts.stream()
                    .filter(p -> !interactedPostIds.contains(p.getId()))
                    .filter(p -> !recommendations.contains(p))
                    .limit(limit - recommendations.size())
                    .forEach(recommendations::add);
        }

        return recommendations.stream().distinct().limit(limit).collect(Collectors.toList());
    }

    /**
     * 基于地理位置的推荐
     */
    public List<Post> getLocationBasedRecommendations(Double lat, Double lng, int limit) {
        if (lat == null || lng == null) {
            return getPopularPosts(limit);
        }

        // 简单实现：返回所有有位置信息的内容，按距离排序（这里简化处理）
        List<Post> posts = postRepository.findAll().stream()
                .filter(p -> p.getLat() != null && p.getLng() != null)
                .sorted((a, b) -> {
                    // 计算距离（简化版，实际应该使用Haversine公式）
                    double distA = Math.sqrt(Math.pow(a.getLat() - lat, 2) + Math.pow(a.getLng() - lng, 2));
                    double distB = Math.sqrt(Math.pow(b.getLat() - lat, 2) + Math.pow(b.getLng() - lng, 2));
                    return Double.compare(distA, distB);
                })
                .limit(limit)
                .collect(Collectors.toList());

        return posts;
    }

    /**
     * 热门内容推荐
     */
    public List<Post> getPopularPosts(int limit) {
        Page<Post> page = postRepository.findAll(
                PageRequest.of(0, limit, Sort.by(
                        Sort.Order.desc("viewCount"),
                        Sort.Order.desc("createdAt")
                ))
        );
        return page.getContent();
    }

    /**
     * 热门内容（综合评分）
     */
    public List<Post> getHotPosts(int limit) {
        // 综合浏览量、点赞数、评论数、收藏数
        List<Post> allPosts = postRepository.findAll();
        
        return allPosts.stream()
                .map(post -> {
                    long likeCount = likeRepository.countByPost(post);
                    long commentCount = commentRepository.countByPost(post);
                    long favoriteCount = favoriteRepository.countByPost(post);
                    
                    // 计算热度分数：浏览量*1 + 点赞*10 + 评论*5 + 收藏*8
                    double score = post.getViewCount() * 1.0 +
                                  likeCount * 10.0 +
                                  commentCount * 5.0 +
                                  favoriteCount * 8.0;
                    
                    return new AbstractMap.SimpleEntry<>(post, score);
                })
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 最新内容
     */
    public List<Post> getLatestPosts(int limit) {
        Page<Post> page = postRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return page.getContent();
    }
}

