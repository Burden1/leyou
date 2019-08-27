package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 1.创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        Long orderId = orderService.createOrder(orderDTO);
        return ResponseEntity.ok(orderId);
    }

    /**
     * 2.根据id查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 3.创建订单支付链接
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }

    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryOrderState(orderId).getValue());
    }
}