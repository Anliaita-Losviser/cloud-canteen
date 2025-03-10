package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {
    
    @Resource(name = "reportServiceImpl")
    private ReportService reportService;
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end
    ){
        log.info("营业额统计");
        return Result.success(reportService.getTurnoverStatistics(begin,end));
    }
    
    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end
    ){
        log.info("用户统计");
        return Result.success(reportService.getUserStatistics(begin,end));
    }
    
    /**
     * 获取订单统计信息
     *
     * 该接口用于获取指定日期范围内的订单统计数据，包括但不限于订单数量、总金额等信息
     * 主要作用是为管理层提供决策支持，通过分析订单数据来优化业务策略
     *
     * @param begin 开始日期，格式为"yyyy-MM-dd"，表示统计的起始日期
     * @param end 结束日期，格式为"yyyy-MM-dd"，表示统计的结束日期
     * @return 返回一个Result对象，其中包含OrderReportVO类型的对象，该对象承载订单统计信息
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end
    ){
        // 记录日志信息，标识订单统计操作的开始
        log.info("订单统计");
        // 调用reportService的getOrderStatistics方法获取订单统计信息，并返回成功结果
        return Result.success(reportService.getOrderStatistics(begin,end));
    }
    
    /**
     * 获取销量排名前10的报告
     * 该接口用于获取指定日期范围内销量排名前10的商品信息
     *
     * @param begin 开始日期，格式为yyyy-MM-dd
     * @param end 结束日期，格式为yyyy-MM-dd
     * @return 返回一个Result对象，其中包含SalesTop10ReportVO类型的销量排名信息
     */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end
    ){
        // 记录日志信息，表明开始处理销量排名请求
        log.info("销量排名");
        
        // 调用reportService的getSalesTop10方法获取销量排名前10的商品信息，并返回成功结果
        return Result.success(reportService.getSalesTop10(begin,end));
    }
    
    @GetMapping("/export")
    public void exportBusinessData(HttpServletResponse response){
        reportService.exportBusinessData(response);
    }
}
