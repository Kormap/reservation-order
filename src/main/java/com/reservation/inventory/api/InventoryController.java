package com.reservation.inventory.api;

import com.reservation.inventory.application.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public InventoryDTO.InventoryResponse get(@PathVariable Long productId) {
        return inventoryService.get(productId);
    }

    @PatchMapping("/{productId}")
    public InventoryDTO.InventoryResponse changeQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryDTO.ChangeQuantityRequest request
    ) {
        return inventoryService.changeQuantity(productId, request.quantity());
    }
}
