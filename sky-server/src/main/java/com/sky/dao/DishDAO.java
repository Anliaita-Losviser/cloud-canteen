package com.sky.dao;

import com.sky.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishDAO extends JpaRepository<Dish,Long> {
    Dish findByName(String name);
}
