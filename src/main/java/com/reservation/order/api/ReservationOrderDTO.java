package com.reservation.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ReservationOrderDTO {

    private ReservationOrderDTO() {
    }

    public record CreateRequest(@NotEmpty List<@Valid ItemRequest> items) {
    }

    public record ItemRequest(@NotNull Long productId, @Positive int quantity) {
    }

    public record ItemResponse(
            Long productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineAmount
    ) {
    }

    public record OrderResponse(
            Long id,
            String status,
            BigDecimal totalAmount,
            Instant createdAt,
            Instant cancelledAt,
            List<ItemResponse> items
    ) {
    }
}
