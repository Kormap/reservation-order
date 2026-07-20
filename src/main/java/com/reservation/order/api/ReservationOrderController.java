package com.reservation.order.api;

import com.reservation.order.application.ReservationOrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class ReservationOrderController {

    private final ReservationOrderService orderService;

    public ReservationOrderController(ReservationOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ReservationOrderDTO.OrderResponse> reserve(
            Authentication authentication,
            @Valid @RequestBody ReservationOrderDTO.CreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.reserve(authentication.getName(), request));
    }

    @GetMapping
    public List<ReservationOrderDTO.OrderResponse> getMine(Authentication authentication) {
        return orderService.getMine(authentication.getName());
    }

    @GetMapping("/{orderId}")
    public ReservationOrderDTO.OrderResponse get(Authentication authentication, @PathVariable Long orderId) {
        return orderService.get(authentication.getName(), orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public ReservationOrderDTO.OrderResponse cancel(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        return orderService.cancel(authentication.getName(), orderId);
    }
}
