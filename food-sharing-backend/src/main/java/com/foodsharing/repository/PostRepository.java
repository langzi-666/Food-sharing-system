package com.foodsharing.repository;

import com.foodsharing.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    @Query("SELECT p FROM Post p WHERE p.author.id IN :userIds")
    Page<Post> findByAuthorIdIn(Set<Long> userIds, Pageable pageable);
    
    Page<Post> findByStatus(String status, Pageable pageable);
}
