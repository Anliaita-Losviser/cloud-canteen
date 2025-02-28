package com.sky.mapStruct;

import com.sky.dto.EmployeeDTO;
import com.sky.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EmployeeMapStruct {
    EmployeeMapStruct instance = Mappers.getMapper(EmployeeMapStruct.class);
    
    /**
     * 实体类转换为DTO
     * @param employee
     * @return
     */
    EmployeeDTO convertToEmployeeDTO(Employee employee);
    
    /**
     * DTO转换为实体
     * @param employeeDTO
     * @return
     */
    Employee convertToEmployees(EmployeeDTO employeeDTO);
}
