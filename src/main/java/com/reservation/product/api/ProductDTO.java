package com.reservation.product.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class ProductDTO {

    private ProductDTO() {
    }

    @Schema(name = "ProductCreateRequest")
    public record CreateRequest(
            @NotBlank @Size(max = 150) String name,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            @PositiveOrZero int initialStock
    ) {
    }

    public record ProductResponse(Long id, String name, BigDecimal price, boolean active, int stock) {
    }
}
