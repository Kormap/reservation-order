package com.reservation.order.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ReservationOrderDTO {

    private ReservationOrderDTO() {
    }

    @Schema(name = "ReservationOrderCreateRequest")
    public record CreateRequest(
            @NotBlank @Size(max = 255) String deliveryAddress,
            @NotEmpty List<@Valid ItemRequest> items
    ) {
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
            String deliveryAddress,
            Instant createdAt,
            Instant cancelledAt,
            List<ItemResponse> items
    ) {
    }
}
