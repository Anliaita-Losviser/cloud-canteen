package com.sky.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐菜品关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "setmeal_dish")
public class SetmealDish implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "snowflake-id",strategy = "com.sky.service.impl.IdGenerateServiceImpl")
    @GeneratedValue(generator = "snowflake-id")
    private Long id;

    //套餐id
    @Column(name = "setmeal_id")
    private Long setmealId;

    //菜品id
    @Column(name = "dish_id")
    private Long dishId;

    //菜品名称 （冗余字段）
    @Column(name = "name", length = 32)
    private String name;

    //菜品原价
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    //份数
    @Column(name = "copies")
    private Integer copies;
}
