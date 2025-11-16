package com.foodsharing.repository;

import com.foodsharing.entity.Comment;
import com.foodsharing.entity.Post;
import com.foodsharing.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostAndParentIsNullOrderByCreatedAtDesc(Post post, Pageable pageable);
    
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);
    
    List<Comment> findByUser(User user);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post")
    long countByPost(Post post);
}
