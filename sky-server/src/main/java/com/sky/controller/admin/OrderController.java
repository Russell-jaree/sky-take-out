package com.sky.controller.admin;

import com.github.pagehelper.PageHelper;
import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

//    输入订单号/手机号进行搜索，支持模糊搜索
//    根据订单状态进行筛选
//    下单时间进行时间筛选
//    搜索内容为空，提示未找到相关订单
//    搜索结果页，展示包含搜索关键词的内容
//    分页展示搜索到的订单数据

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){

       PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);

       return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 商家查看订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> details (@PathVariable Long id){

//        订单详情页面需要展示订单基本信息（状态、订单号、下单时间、收货人、电话、收货地址、金额等）
//        订单详情页面需要展示订单明细数据（商品名称、数量、单价）
        OrderVO orderVO = orderService.details(id);

        return Result.success(orderVO);
    }

    /**
     * 接单
     * 商家接单其实就是将订单的状态修改为“已接单”
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){

        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒绝接单
     * 商家拒单其实就是将订单状态修改为“已取消”
     * 只有订单处于“待接单”状态时可以执行拒单操作
     * 商家拒单时需要指定拒单原因
     * 商家拒单时，如果用户已经完成了支付，需要为用户退款
     */
    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO)
    throws Exception{

        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO)throws Exception{

        orderService.cancel(ordersCancelDTO);
        return Result.success();

    }

    /**
     * 派送订单
     *
     * @return
     */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable("id") Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     *
     * @return
     */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }




}
