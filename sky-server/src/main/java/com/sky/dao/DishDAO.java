package com.sky.dao;

import com.sky.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishDAO extends JpaRepository<Dish,Long> {
    Dish findByName(String name);
    List<Dish> findByCategoryId(Long categoryId);
}
