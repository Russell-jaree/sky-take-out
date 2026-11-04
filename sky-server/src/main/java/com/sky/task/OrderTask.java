package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类。定时处理订单状态
 */
@Slf4j
@Component
public class OrderTask {

    /**
     * 处理超时订单
     */
    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? ") //每分钟处理一次
    public void processTimeOutOrder(){

        log.info("定时处理超时订单：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //select * from order where status = ? and order_time < (now-15)
         List<Orders> ordersList = orderMapper.getByStausAndOrderTimeLT(Orders.PENDING_PAYMENT,time);

         if(!ordersList.isEmpty()){

             for (Orders orders : ordersList) {
                 orders.setStatus(Orders.CANCELLED);
                 orders.setCancelReason("订单超时，自动取消");
                 orders.setCancelTime(LocalDateTime.now());
                 orderMapper.update(orders);
             }
         }
    }

    @Scheduled(cron = "0 0 1 * * ? ")//每天凌晨1点处理
    public void processDeliveryOrder(){

        log.info("定时处理派送中的订单：{}",LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStausAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if(!ordersList.isEmpty()){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
