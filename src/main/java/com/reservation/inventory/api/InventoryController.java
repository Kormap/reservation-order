package com.reservation.inventory.api;

import com.reservation.inventory.application.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories")
@Tag(name = "04. 재고", description = "상품별 가용 재고 조회 및 관리 API")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "재고 조회", description = "상품 ID로 현재 가용 재고 수량을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재고 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "재고를 찾을 수 없음")
    })
    public InventoryDTO.InventoryResponse get(@Parameter(description = "상품 ID", example = "1") @PathVariable Long productId) {
        return inventoryService.get(productId);
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "재고 수량 변경", description = "관리자만 상품의 가용 재고 수량을 변경할 수 있습니다. CSRF 토큰이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재고 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 또는 CSRF 토큰 없음"),
            @ApiResponse(responseCode = "404", description = "재고를 찾을 수 없음")
    })
    public InventoryDTO.InventoryResponse changeQuantity(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
            @Valid @RequestBody InventoryDTO.ChangeQuantityRequest request
    ) {
        return inventoryService.changeQuantity(productId, request.quantity());
    }
}
