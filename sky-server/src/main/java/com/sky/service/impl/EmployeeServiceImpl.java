package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dao.EmployeeDAO;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapStruct.EmployeeMapStruct;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    @Resource(name = "employeeDAO")
    private EmployeeDAO employeeDAO;
    
    @Resource(name = "employeeMapper")
    private EmployeeMapper employeeMapper;
    
    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();
        //进行MD5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        
        //1、根据用户名查询数据库中的数据
        Employee employee = employeeDAO.findByUsername(username);
        
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        
        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        
        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        
        //3、返回实体对象
        return employee;
    }
    
    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee, selectEmployee;
        //对象属性拷贝，DTO转实体
        employee = EmployeeMapStruct.instance.convertToEmployees(employeeDTO);
        
        //先根据username查询用户，查到了说明数据库里面有了，报异常
        selectEmployee = employeeDAO.findByUsername(employee.getUsername());
        if (selectEmployee != null) {
            //账号存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_ALREADY_EXIST);
        }
        //生成id
        //employee.setId(idGenerateService.generateUserId());
        //设置账号状态，ENABLE为启用
        employee.setStatus(StatusConstant.ENABLE);
        //设置密码，默认123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置创建时间和修改时间
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //设置创建人ID和修改人ID为当前登录用户的ID
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //插入表中
        employeeDAO.save(employee);
        //释放ThreadLocal
        BaseContext.removeCurrentId();
    }
    
    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> pages = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = pages.getTotal();
        List<Employee> records = pages.getResult();
        return new PageResult(total, records);
    }
    
    /**
     * 启用或禁用
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //根据ID查询
        Optional<Employee> employee = employeeDAO.findById(id);
        //设置状态
        employee.get().setStatus(status);
        //更新字段
        employeeDAO.save(employee.get());
    }
    
    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        //根据ID查询
        Optional<Employee> employee = employeeDAO.findById(id);
        employee.get().setPassword("******");
        return employee.get();
    }
    
    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        //获得更新后的信息
        Employee updatedEmployee = EmployeeMapStruct.instance.convertToEmployees(employeeDTO);
        //查找更新前的信息
        Employee previousEmployee = employeeDAO.findById(updatedEmployee.getId()).get();
        //更新字段
        previousEmployee.setUsername(updatedEmployee.getUsername());
        previousEmployee.setName(updatedEmployee.getName());
        previousEmployee.setPhone(updatedEmployee.getPhone());
        previousEmployee.setSex(updatedEmployee.getSex());
        previousEmployee.setIdNumber(updatedEmployee.getIdNumber());
        //previousEmployee.setUpdateTime(LocalDateTime.now());
        previousEmployee.setUpdateUser(BaseContext.getCurrentId());
        
        log.info("即将更新：{}", previousEmployee);
        employeeDAO.save(previousEmployee);
    }
}
