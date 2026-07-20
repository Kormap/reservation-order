package com.reservation.inventory.api;

import jakarta.validation.constraints.PositiveOrZero;

public final class InventoryDTO {

    private InventoryDTO() {
    }

    public record ChangeQuantityRequest(@PositiveOrZero int quantity) {
    }

    public record InventoryResponse(Long productId, int quantity) {
    }
}
