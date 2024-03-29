package com.devsei.orderservice.ui;

import com.devsei.orderservice.application.OrderService;
import com.devsei.orderservice.dto.OrderReq;
import com.devsei.orderservice.dto.OrderRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/order-service")
@Slf4j
@RestController
public class OrderController {
    private final OrderService orderService;
    private final Environment environment;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in Order Service on PORT %s",
                environment.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<OrderRes> createOrder(@RequestBody OrderReq req,
                                                @PathVariable("userId") String userId) {
        log.info("Before retrieve orders data");
        OrderRes res = orderService.createOrder(req, userId);
        log.info("After retrieve orders data");
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<OrderRes>> getOrder(@PathVariable("userId") String userId) {
        log.info("Before retrieve orders data");
        List<OrderRes> res = new ArrayList<>();
        orderService.findAllByUserId(userId).forEach(order -> res.add(OrderRes.from(order)));
        log.info("After retrieve orders data");
        return ResponseEntity.ok(res);
    }
}
