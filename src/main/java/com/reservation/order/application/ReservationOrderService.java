package com.reservation.order.application;

import com.reservation.common.exception.BusinessException;
import com.reservation.common.exception.ErrorCode;
import com.reservation.inventory.domain.Inventory;
import com.reservation.inventory.domain.InventoryRepository;
import com.reservation.member.domain.Member;
import com.reservation.member.domain.MemberRepository;
import com.reservation.order.api.ReservationOrderDTO.CreateRequest;
import com.reservation.order.api.ReservationOrderDTO.ItemRequest;
import com.reservation.order.api.ReservationOrderDTO.ItemResponse;
import com.reservation.order.api.ReservationOrderDTO.OrderResponse;
import com.reservation.order.domain.ReservationOrder;
import com.reservation.order.domain.ReservationOrderItem;
import com.reservation.order.domain.ReservationOrderRepository;
import com.reservation.product.domain.Product;
import com.reservation.product.domain.ProductRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationOrderService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ReservationOrderRepository orderRepository;

    public ReservationOrderService(MemberRepository memberRepository, ProductRepository productRepository,
                                   InventoryRepository inventoryRepository,
                                   ReservationOrderRepository orderRepository) {
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse reserve(String email, CreateRequest request) {
        validateDuplicateProducts(request.items());
        Member member = findMember(email);
        ReservationOrder order = new ReservationOrder(member);

        for (ItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            if (!product.isActive()) {
                throw new BusinessException(ErrorCode.PRODUCT_INACTIVE);
            }
            Inventory inventory = findInventory(product.getId());
            inventory.decrease(itemRequest.quantity());
            order.addItem(product.getId(), product.getName(), product.getPrice(), itemRequest.quantity());
        }

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancel(String email, Long orderId) {
        ReservationOrder order = findOrder(email, orderId);
        order.cancel();
        for (ReservationOrderItem item : order.getItems()) {
            findInventory(item.getProductId()).increase(item.getQuantity());
        }
        return toResponse(order);
    }

    public OrderResponse get(String email, Long orderId) {
        return toResponse(findOrder(email, orderId));
    }

    public List<OrderResponse> getMine(String email) {
        return orderRepository.findAllByMemberEmailOrderByCreatedAtDesc(email).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateDuplicateProducts(List<ItemRequest> items) {
        Set<Long> productIds = new HashSet<>();
        if (items.stream().anyMatch(item -> !productIds.add(item.productId()))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private Member findMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Inventory findInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
    }

    private ReservationOrder findOrder(String email, Long orderId) {
        return orderRepository.findByIdAndMemberEmail(orderId, email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private OrderResponse toResponse(ReservationOrder order) {
        List<ItemResponse> items = order.getItems().stream()
                .map(item -> new ItemResponse(item.getProductId(), item.getProductName(), item.getUnitPrice(),
                        item.getQuantity(), item.getLineAmount()))
                .toList();
        return new OrderResponse(order.getId(), order.getStatus().name(), order.getTotalAmount(),
                order.getCreatedAt(), order.getCancelledAt(), items);
    }
}
