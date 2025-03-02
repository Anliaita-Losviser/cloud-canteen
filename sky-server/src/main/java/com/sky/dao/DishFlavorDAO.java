package com.sky.dao;

import com.sky.entity.DishFlavor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishFlavorDAO extends JpaRepository<DishFlavor,Long> {
    void deleteAllInBatchByDishId(Long dishId);
    List<DishFlavor> findByDishId(Long dishId);
}
