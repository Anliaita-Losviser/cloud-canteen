package com.sky.dao;

import com.sky.entity.Setmeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetmealDAO extends JpaRepository<Setmeal,Long> {
    Setmeal findByName(String name);
}
