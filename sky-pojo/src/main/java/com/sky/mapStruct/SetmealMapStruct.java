package com.sky.mapStruct;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.SetmealVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SetmealMapStruct {
    SetmealMapStruct instance = Mappers.getMapper(SetmealMapStruct.class);
    
    /**
     * DTO转实体
     * @param setmealDTO
     * @return
     */
    Setmeal convertToSetmeal(SetmealDTO setmealDTO);
    
    /**
     * 实体类转VO
     * @param setmeal
     * @return
     */
    SetmealVO convertToSetmealVO(Setmeal setmeal);
}
