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
            @Size(max = 2000) String description,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            @Schema(
                    description = "상품 대분류 코드",
                    example = "STATIONERY",
                    allowableValues = {
                            "APPLIANCE", "GENERAL_MERCHANDISE", "STATIONERY", "BOOK", "FASHION",
                            "FOOD", "BEAUTY", "SPORTS_LEISURE", "DIGITAL"
                    }
            )
            @NotBlank @Size(max = 50) String categoryCode,
            @PositiveOrZero int initialStock
    ) {
    }

    public record UpdateRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 2000) String description,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            @Schema(
                    description = "상품 대분류 코드",
                    example = "STATIONERY",
                    allowableValues = {
                            "APPLIANCE", "GENERAL_MERCHANDISE", "STATIONERY", "BOOK", "FASHION",
                            "FOOD", "BEAUTY", "SPORTS_LEISURE", "DIGITAL"
                    }
            )
            @NotBlank @Size(max = 50) String categoryCode,
            @NotNull Boolean active
    ) {
    }

    public record ProductResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            String categoryCode,
            String categoryName,
            boolean active,
            int stock,
            java.time.Instant createdAt,
            java.time.Instant updatedAt
    ) {
    }
}
