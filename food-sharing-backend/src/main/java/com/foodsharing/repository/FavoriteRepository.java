package com.foodsharing.repository;

import com.foodsharing.entity.Favorite;
import com.foodsharing.entity.Post;
import com.foodsharing.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserAndPost(User user, Post post);
    
    boolean existsByUserAndPost(User user, Post post);
    
    void deleteByUserAndPost(User user, Post post);
    
    Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.post = :post")
    long countByPost(Post post);
}
