package com.sky.dao;

import com.sky.entity.SetmealDish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SetmealDishDAO extends JpaRepository<SetmealDish,Long> {
    List<SetmealDish> findByDishIdIn(List<Long> dishIds);
    List<SetmealDish> findBySetmealId(Long setmealId);
    @Query("select sd.setmealId from SetmealDish sd where sd.dishId in :dishIds")
    List<Long> findSetmealIdsByDishIds(@Param("dishIds") List<Long> dishIds);
    
    void deleteAllInBatchBySetmealId(Long setmealId);
}
