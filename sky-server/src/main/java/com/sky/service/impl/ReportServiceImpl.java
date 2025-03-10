package com.sky.service.impl;

import com.sky.dao.OrderDAO;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    
    @Resource(name = "orderDAO")
    private OrderDAO orderDAO;
    @Resource(name = "orderMapper")
    private OrderMapper orderMapper;
    @Resource(name = "userMapper")
    private UserMapper userMapper;
    @Resource(name = "workspaceServiceImpl")
    private WorkspaceService workspaceService;
    /**
     * 统计营业额
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 从begin到end的日期，查询对应的营业额数据
        List<LocalDate> dateList = getBetweenDates(begin, end);
        
        List<Double> turnoverList = new ArrayList<>();
        //统计营业额
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            //select sum(amount) from orders where order_time between beginTime and endTime and status = 5
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }
    
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 从begin到end的日期，查询对应的用户数据
        List<LocalDate> dateList = getBetweenDates(begin, end);
        
        //总用户数
        //select count(id) from user where create_time between begin and end
        List<Integer> totalUserList = new ArrayList<>();
        //新增用户数
        //select count(id) from user where create_time < ?
        List<Integer> newUserList = new ArrayList<>();
        
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            //先统计总数
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            //再统计新增
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }
    
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 从begin到end的日期，查询对应的用户数据
        List<LocalDate> dateList = getBetweenDates(begin, end);
        
        // 查询订单总数
        List<Integer> orderCountList = new ArrayList<>();
        // 查询有效订单总数
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            
            //每天总订单数
            //select count(id) from orders where order_time > ? and order_time < ?
            Integer allOrderCount = orderMapper.countByMap(map);
            orderCountList.add(allOrderCount);
            //每天有效订单数
            //select count(id) from orders where order_time > ? and order_time < ? and status = 5
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }
        //计算时间区间内的订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算时间区间内的有效订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate = 0.0;
        
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue()/totalOrderCount.doubleValue();
        }
        
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
    
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        if (salesTop10 != null && !salesTop10.isEmpty()) {
            String nameList = StringUtils.join(salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ",");
            String numberList = StringUtils.join(salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ",");
            return SalesTop10ReportVO
                    .builder()
                    .nameList(nameList)
                    .numberList(numberList)
                    .build();
        }
        return null;
    }
    
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 1、查询30天内数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));
        
        // 2、写入excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            // 获取sheet
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填时间
            sheet.getRow(1).getCell(1).setCellValue("时间："+dateBegin + "至" + dateEnd);
            //填概览数据
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());
            
            //填明细数据
            for (int i = 0; i < 30; i++){
                LocalDate date = dateBegin.plusDays(i);
                // 获取该日期的营业额等数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN),
                LocalDateTime.of(date, LocalTime.MAX));
                // 填入到excel中
                sheet.getRow(7 + i).getCell(1).setCellValue(date.toString());
                sheet.getRow(7 + i).getCell(2).setCellValue(businessData.getTurnover());
                sheet.getRow(7 + i).getCell(3).setCellValue(businessData.getValidOrderCount());
                sheet.getRow(7 + i).getCell(4).setCellValue(businessData.getOrderCompletionRate());
                sheet.getRow(7 + i).getCell(5).setCellValue(businessData.getUnitPrice());
                sheet.getRow(7 + i).getCell(6).setCellValue(businessData.getNewUsers());
            }
            
            // 3、输出到浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private List<LocalDate> getBetweenDates(LocalDate begin, LocalDate end) {
        List<LocalDate> result = new ArrayList<>();
        result.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            result.add(begin);
        }
        return result;
    }
}
