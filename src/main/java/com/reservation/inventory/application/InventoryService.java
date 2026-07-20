package com.reservation.inventory.application;

import com.reservation.common.exception.BusinessException;
import com.reservation.common.exception.ErrorCode;
import com.reservation.inventory.api.InventoryDTO.InventoryResponse;
import com.reservation.inventory.domain.Inventory;
import com.reservation.inventory.domain.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryResponse get(Long productId) {
        return toResponse(findInventory(productId));
    }

    @Transactional
    public InventoryResponse changeQuantity(Long productId, int quantity) {
        Inventory inventory = findInventory(productId);
        inventory.changeQuantity(quantity);
        return toResponse(inventory);
    }

    private Inventory findInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(inventory.getProduct().getId(), inventory.getQuantity());
    }
}
