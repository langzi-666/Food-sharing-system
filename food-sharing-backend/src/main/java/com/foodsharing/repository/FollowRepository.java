package com.foodsharing.repository;

import com.foodsharing.entity.Follow;
import com.foodsharing.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    void deleteByFollowerAndFollowing(User follower, User following);
    
    Page<Follow> findByFollowerOrderByCreatedAtDesc(User follower, Pageable pageable);
    
    Page<Follow> findByFollowingOrderByCreatedAtDesc(User following, Pageable pageable);
    
    List<Follow> findByFollower(User follower);
    
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following = :user")
    long countFollowers(User user);
    
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower = :user")
    long countFollowing(User user);
}
