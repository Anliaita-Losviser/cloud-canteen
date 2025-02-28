package com.sky.mapStruct;

import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoryMapStruct {
    CategoryMapStruct instance = Mappers.getMapper(CategoryMapStruct.class);
    
    /**
     * DTO转换为实体
     * @param categoryDTO
     * @return
     */
    Category convertToCategory(CategoryDTO categoryDTO);
    
    /**
     * 实体类转换为DTO
     * @param category
     * @return
     */
    CategoryDTO convertToCategoryDTO(Category category);
}
