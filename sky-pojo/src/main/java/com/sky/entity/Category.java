package com.sky.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "snowflake-id",strategy = "com.sky.service.impl.IdGenerateServiceImpl")
    @GeneratedValue(generator = "snowflake-id")
    private Long id;

    //类型: 1菜品分类 2套餐分类
    @Column(name = "type")
    private Integer type;

    //分类名称
    @Column(name = "name", length = 32)
    private String name;

    //顺序
    @Column(name = "sort")
    private Integer sort;

    //分类状态 0标识禁用 1表示启用
    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    @CreatedDate
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @LastModifiedDate
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Column(name = "create_user")
    private Long createUser;

    @Column(name = "update_user")
    private Long updateUser;
}
