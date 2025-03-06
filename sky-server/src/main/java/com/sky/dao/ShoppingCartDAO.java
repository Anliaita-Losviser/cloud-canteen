package com.sky.dao;

import com.sky.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingCartDAO extends JpaRepository<ShoppingCart,Long> {
    List<ShoppingCart> findByUserId(Long userId);
    void deleteAllInBatchByUserId(Long userId);
}
