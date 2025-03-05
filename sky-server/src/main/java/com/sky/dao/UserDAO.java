package com.sky.dao;

import com.sky.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User,Long> {
    User findByOpenid(String openid);
}
