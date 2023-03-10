package boot.basic.api;

import boot.basic.domain.Address;
import boot.basic.domain.Order;
import boot.basic.domain.OrderItem;
import boot.basic.domain.OrderStatus;
import boot.basic.repository.OrderRepository;
import boot.basic.repository.order.query.OrderFlatDto;
import boot.basic.repository.order.query.OrderQueryDto;
import boot.basic.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;


/**
 * <p>V1. 엔티티 직접 노출
 * <p>- 엔티티가 변하면 API 스펙이 변한다.
 * <p>- 트랜잭션 안에서 지연 로딩 필요
 * <p>- 양방향 연관관계 문제
 *
 * <p>V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * <p>- 트랜잭션 안에서 지연 로딩 필요
 * <p>V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * <p>- 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
 *
 * <p>V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
 * <p>- 페이징 가능
 * <p>V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
 * <p>- 페이징 가능
 * <p>V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * <p>- 페이징 불가능...
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * <p> V1. 엔티티 직접 노출
     * <p>- Hibernate5Module 모듈 등록, LAZY=null 처리
     * <p>- 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기환
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

//        for (Order order : orders) {
//            System.out.println("order ref=\"" + order + "\", id =\"" + order.getId() + "\"");
//        }
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }


    /**
     * <p>V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려</p>
     * <p>- ToOne 관계만 우선 모두 페치 조인으로 최적화</p>
     * <p>- 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> ordersV6() {
        return orderQueryRepository.findAllByDto_flat();
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count;      //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
