package com.sky.mapStruct;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DishMapStruct {
    DishMapStruct instance = Mappers.getMapper(DishMapStruct.class);
    
    /**
     * 实体类转换为DTO
     * @param dish
     * @return
     */
    DishDTO convertToDishDTO(Dish dish);
    /**
     * DTO转换为实体
     * @param dishDTO
     * @return
     */
    Dish convertToDish(DishDTO dishDTO);
    
    /**
     * 实体类转VO
     * @param dish
     * @return
     */
    DishVO convertToDishVO(Dish dish);
}
