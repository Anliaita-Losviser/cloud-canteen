package com.sky.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shopping_cart")
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "snowflake-id",strategy = "com.sky.service.impl.IdGenerateServiceImpl")
    @GeneratedValue(generator = "snowflake-id")
    private Long id;

    //名称
    @Column(name = "name", length = 32)
    private String name;

    //用户id
    @Column(name = "user_id")
    private Long userId;

    //菜品id
    @Column(name = "dish_id")
    private Long dishId;

    //套餐id
    @Column(name = "setmeal_id")
    private Long setmealId;

    //口味
    @Column(name = "dish_flavor",length = 50)
    private String dishFlavor;

    //数量
    @Column(name = "number")
    private Integer number;

    //金额
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    //图片
    @Column(name = "image")
    private String image;

    @Column(name = "create_time")
    @CreatedDate
    private LocalDateTime createTime;
}
