package com.reservation.product.application;

import com.reservation.common.exception.BusinessException;
import com.reservation.common.exception.ErrorCode;
import com.reservation.inventory.domain.Inventory;
import com.reservation.inventory.domain.InventoryRepository;
import com.reservation.product.api.ProductDTO.CreateRequest;
import com.reservation.product.api.ProductDTO.ProductResponse;
import com.reservation.product.domain.Product;
import com.reservation.product.domain.ProductRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public ProductResponse create(CreateRequest request) {
        Product product = productRepository.save(new Product(request.name().trim(), request.price()));
        Inventory inventory = inventoryRepository.save(new Inventory(product, request.initialStock()));
        return toResponse(product, inventory);
    }

    public ProductResponse get(Long productId) {
        Product product = findProduct(productId);
        Inventory inventory = findInventory(productId);
        return toResponse(product, inventory);
    }

    public List<ProductResponse> getAll() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return List.of();
        }
        Map<Long, Inventory> inventoriesByProductId = inventoryRepository
                .findAllByProductIdIn(products.stream().map(Product::getId).toList())
                .stream()
                .collect(Collectors.toMap(inventory -> inventory.getProduct().getId(), Function.identity()));

        return products.stream()
                .map(product -> toResponse(product, requireInventory(inventoriesByProductId, product.getId())))
                .toList();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Inventory findInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
    }

    private Inventory requireInventory(Map<Long, Inventory> inventoriesByProductId, Long productId) {
        Inventory inventory = inventoriesByProductId.get(productId);
        if (inventory == null) {
            throw new BusinessException(ErrorCode.INVENTORY_NOT_FOUND);
        }
        return inventory;
    }

    private ProductResponse toResponse(Product product, Inventory inventory) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(),
                product.isActive(), inventory.getQuantity());
    }
}
