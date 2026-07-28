package com.reservation.order.api;

import com.reservation.order.application.ReservationOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "05. 예약 주문", description = "예약 주문 생성, 조회, 취소 API")
public class ReservationOrderController {

    private final ReservationOrderService orderService;

    public ReservationOrderController(ReservationOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "예약 주문 생성", description = "MEMBER 역할의 회원이 상품별 주문 수량을 전달해 예약 주문을 생성하고 재고를 차감합니다. CSRF 토큰이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "예약 주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "MEMBER 권한 또는 CSRF 토큰 없음"),
            @ApiResponse(responseCode = "404", description = "상품 또는 재고를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "재고 부족 또는 판매 중지 상품")
    })
    public ResponseEntity<ReservationOrderDTO.OrderResponse> reserve(
            Authentication authentication,
            @Valid @RequestBody ReservationOrderDTO.CreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.reserve(authentication.getName(), request));
    }

    @GetMapping
    @Operation(summary = "내 예약 주문 목록 조회", description = "현재 로그인한 회원이 생성한 예약 주문을 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    public List<ReservationOrderDTO.OrderResponse> getMine(Authentication authentication) {
        return orderService.getMine(authentication.getName());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "예약 주문 상세 조회", description = "현재 로그인한 회원이 생성한 예약 주문의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없거나 조회 권한 없음")
    })
    public ReservationOrderDTO.OrderResponse get(Authentication authentication,
                                                  @Parameter(description = "예약 주문 ID", example = "1") @PathVariable Long orderId) {
        return orderService.get(authentication.getName(), orderId);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "예약 주문 취소", description = "현재 로그인한 회원의 예약 주문을 취소하고 차감된 재고를 복구합니다. CSRF 토큰이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 취소 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "CSRF 토큰 누락 또는 권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없거나 취소 권한 없음"),
            @ApiResponse(responseCode = "409", description = "이미 취소된 주문")
    })
    public ReservationOrderDTO.OrderResponse cancel(
            Authentication authentication,
            @Parameter(description = "예약 주문 ID", example = "1") @PathVariable Long orderId
    ) {
        return orderService.cancel(authentication.getName(), orderId);
    }
}
