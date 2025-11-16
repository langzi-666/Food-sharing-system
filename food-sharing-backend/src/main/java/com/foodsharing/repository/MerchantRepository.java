package com.foodsharing.repository;

import com.foodsharing.entity.Merchant;
import com.foodsharing.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long>, JpaSpecificationExecutor<Merchant> {
    Optional<Merchant> findByUser(User user);
    
    Optional<Merchant> findByUserId(Long userId);
    
    Page<Merchant> findByStatus(Integer status, Pageable pageable);
    
    Page<Merchant> findByStatusAndCuisineTypeContaining(Integer status, String cuisineType, Pageable pageable);
    
    @Query("SELECT m FROM Merchant m WHERE m.status = 1 AND " +
           "(:q IS NULL OR m.name LIKE %:q% OR m.description LIKE %:q%)")
    Page<Merchant> searchMerchants(@Param("q") String q, Pageable pageable);
    
    @Query("SELECT m FROM Merchant m WHERE m.status = 1 ORDER BY m.rating DESC, m.reviewCount DESC")
    Page<Merchant> findTopRatedMerchants(Pageable pageable);
}

