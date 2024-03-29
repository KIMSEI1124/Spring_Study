package com.devsei.orderservice.application;

import com.devsei.orderservice.domain.OrderJapEntity;
import com.devsei.orderservice.domain.OrderRepository;
import com.devsei.orderservice.dto.OrderReq;
import com.devsei.orderservice.dto.OrderRes;
import com.devsei.orderservice.messagequeue.KafkaProducer;
import com.devsei.orderservice.vo.OrderVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final KafkaProducer kafkaProducer;

    @Override
    public OrderRes createOrder(OrderReq req, String userId) {
        OrderJapEntity orderJapEntity = OrderJapEntity.builder()
                .productId(req.getProductId())
                .quantity(req.getQuantity())
                .unitPrice(req.getUnitPrice())
                .userId(userId)
                .build();
        OrderJapEntity savedOrderJpaEntity = orderRepository.save(orderJapEntity);

        /* Send this order to the kafka */
        kafkaProducer.send("example-catalog-topic", OrderVo.from(savedOrderJpaEntity));

        return OrderRes.from(savedOrderJpaEntity);
    }

    @Override
    public Iterable<OrderJapEntity> findAllByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public OrderRes getOrderByOrderId(String orderId) {
        return OrderRes.from(orderRepository.findByOrderId(orderId));
    }
}
