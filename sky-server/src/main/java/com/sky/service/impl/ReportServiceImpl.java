package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.ReportQueryDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getByTurnOver(LocalDate begin, LocalDate end) {
        /**
         * 营业额指订单状态为已完成的订单金额合计,营业额，以逗号分隔，例如：406.0,1520.0,75.0
         *
         * 基于可视化报表的折线图展示营业额数据，X轴为日期，Y轴为营业额
         *
         * 根据时间选择区间，展示每天的营业额数据
         */

        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        turnoverReportVO.setDateList(StringUtils.join(dateList,",")); //long3的包

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
//            Map map = new HashMap();
//            map.put("status", Orders.COMPLETED);
//            map.put("begin",beginTime);
//            map.put("end", endTime);
            ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
            reportQueryDTO.setStatus(Orders.COMPLETED);
            reportQueryDTO.setBeginTime(beginTime);
            reportQueryDTO.setEndTime(endTime);

            Double turnover = orderMapper.sumByReport(reportQueryDTO);

            turnover = turnover == null ? 0 : turnover ;
            turnoverList.add(turnover);
        }

        turnoverReportVO.setTurnoverList( StringUtils.join(turnoverList,",") );

        return turnoverReportVO;
    }

    /**
     * //日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
     *     private String dateList;
     *
     *     //用户总量，以逗号分隔，例如：200,210,220
     *     private String totalUserList;
     *
     *     //新增用户，以逗号分隔，例如：20,21,10
     *     private String newUserList;
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        UserReportVO userReportVO = new UserReportVO();

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        userReportVO.setDateList(StringUtils.join(dateList,","));

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //新增用户
            //select count(id) from user where create_time >begin and create_time < end
            newUserList.add(getUserCount(beginTime,endTime));

            totalUserList.add(getUserCount(null,endTime));
        }

        userReportVO.setNewUserList(StringUtils.join(newUserList,","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList,","));
        return userReportVO;
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {

        //时间列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //订单数 列表
        List<Integer> orderCountList = new ArrayList<>();
        //订单总数
        //有效订单数
        //有效列表
        List<Integer> validOrderCountList = new ArrayList<>();
        //订单完成率

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //订单总数 select count(id) from order where crreatetime < endtime
            orderCountList.add(getOrderCount(beginTime,endTime,null));

            //有效订单数 select count(id )from order where ct < endtime and status = ?
            validOrderCountList.add(getOrderCount(beginTime,endTime,Orders.COMPLETED));
        }

        //订单总数,用stream流来写，reduce是用来合并的
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //有效总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount ;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        //namelist 商品名称列表
        //numberList 销量列表
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime,endTime);

        //map 方法是获取每个名字为xxx的元素
        String nameList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ",");
        String numberList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        return orderMapper.countByOrder(beginTime,endTime,status);
    }


    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {

        return userMapper.countByTime(beginTime,endTime);
    }
}
