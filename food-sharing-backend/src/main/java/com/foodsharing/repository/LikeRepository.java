package com.foodsharing.repository;

import com.foodsharing.entity.Like;
import com.foodsharing.entity.Post;
import com.foodsharing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndPost(User user, Post post);
    
    boolean existsByUserAndPost(User user, Post post);
    
    void deleteByUserAndPost(User user, Post post);
    
    List<Like> findByUser(User user);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post = :post")
    long countByPost(Post post);
}
