package com.reservation.product.application;

import com.reservation.common.exception.BusinessException;
import com.reservation.common.exception.ErrorCode;
import com.reservation.category.domain.Category;
import com.reservation.category.domain.CategoryRepository;
import com.reservation.inventory.domain.Inventory;
import com.reservation.inventory.domain.InventoryRepository;
import com.reservation.product.api.ProductDTO.CreateRequest;
import com.reservation.product.api.ProductDTO.ProductResponse;
import com.reservation.product.api.ProductDTO.UpdateRequest;
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
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductResponse create(CreateRequest request) {
        Category category = findCategory(request.categoryCode());
        Product product = productRepository.save(new Product(request.name().trim(), request.description(), request.price(),
                category.getCode()));
        Inventory inventory = inventoryRepository.save(new Inventory(product, request.initialStock()));
        return toResponse(product, inventory, category.getName());
    }

    @Transactional
    public ProductResponse update(Long productId, UpdateRequest request) {
        Product product = findProduct(productId);
        Category category = findCategory(request.categoryCode());
        product.update(request.name().trim(), request.description(), request.price(), category.getCode(), request.active());
        return toResponse(product, findInventory(productId), category.getName());
    }

    public ProductResponse get(Long productId) {
        Product product = findProduct(productId);
        Inventory inventory = findInventory(productId);
        return toResponse(product, inventory, findCategory(product.getCategoryCode()).getName());
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
        Map<String, String> categoryNamesByCode = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getCode, Category::getName));

        return products.stream()
                .map(product -> toResponse(product, requireInventory(inventoriesByProductId, product.getId()),
                        requireCategoryName(categoryNamesByCode, product.getCategoryCode())))
                .toList();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Category findCategory(String categoryCode) {
        return categoryRepository.findById(categoryCode.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
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

    private String requireCategoryName(Map<String, String> categoryNamesByCode, String categoryCode) {
        String categoryName = categoryNamesByCode.get(categoryCode);
        if (categoryName == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return categoryName;
    }

    private ProductResponse toResponse(Product product, Inventory inventory, String categoryName) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getCategoryCode(), categoryName, product.isActive(), inventory.getQuantity(),
                product.getCreatedAt(), product.getUpdatedAt());
    }
}
