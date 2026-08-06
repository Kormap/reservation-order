package com.reservation.product.api;

import com.reservation.product.application.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "03. 상품", description = "상품 등록 및 조회 API")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "상품 등록", description = "관리자만 상품과 초기 재고를 함께 등록할 수 있습니다. CSRF 토큰이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 또는 CSRF 토큰 없음")
    })
    public ResponseEntity<ProductDTO.ProductResponse> create(@Valid @RequestBody ProductDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "관리자만 상품 정보와 판매 상태를 수정할 수 있습니다. CSRF 토큰이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 또는 CSRF 토큰 없음"),
            @ApiResponse(responseCode = "404", description = "상품 또는 카테고리를 찾을 수 없음")
    })
    public ProductDTO.ProductResponse update(@Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
                                             @Valid @RequestBody ProductDTO.UpdateRequest request) {
        return productService.update(productId, request);
    }

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "등록된 모든 상품과 각 상품의 현재 재고를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    public List<ProductDTO.ProductResponse> getAll() { return productService.getAll(); }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 정보와 현재 재고를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "상품 또는 재고를 찾을 수 없음")
    })
    public ProductDTO.ProductResponse get(@Parameter(description = "상품 ID", example = "1") @PathVariable Long productId) {
        return productService.get(productId);
    }
}
