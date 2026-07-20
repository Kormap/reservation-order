COMMENT ON TABLE members IS '서비스 회원 정보';
COMMENT ON COLUMN members.id IS '회원 식별자';
COMMENT ON COLUMN members.email IS '로그인에 사용하는 이메일 주소';
COMMENT ON COLUMN members.password IS 'BCrypt 등으로 암호화된 비밀번호 해시';
COMMENT ON COLUMN members.name IS '회원 표시 이름';
COMMENT ON COLUMN members.role IS '회원 권한 역할(MEMBER, ADMIN)';
COMMENT ON COLUMN members.created_at IS '회원 가입 일시(UTC)';

COMMENT ON TABLE products IS '판매 상품 정보';
COMMENT ON COLUMN products.id IS '상품 식별자';
COMMENT ON COLUMN products.name IS '상품명';
COMMENT ON COLUMN products.price IS '상품 판매 단가';
COMMENT ON COLUMN products.active IS '판매 가능 여부';
COMMENT ON COLUMN products.created_at IS '상품 등록 일시(UTC)';

COMMENT ON TABLE inventories IS '상품별 현재 재고 수량';
COMMENT ON COLUMN inventories.id IS '재고 식별자';
COMMENT ON COLUMN inventories.product_id IS '재고를 보유한 상품 식별자';
COMMENT ON COLUMN inventories.quantity IS '현재 가용 재고 수량';

COMMENT ON TABLE reservation_orders IS '회원의 예약 주문 기본 정보';
COMMENT ON COLUMN reservation_orders.id IS '예약 주문 식별자';
COMMENT ON COLUMN reservation_orders.member_id IS '예약 주문을 생성한 회원 식별자';
COMMENT ON COLUMN reservation_orders.status IS '예약 주문 상태(RESERVED, CANCELLED)';
COMMENT ON COLUMN reservation_orders.total_amount IS '예약 주문 전체 금액';
COMMENT ON COLUMN reservation_orders.created_at IS '예약 주문 생성 일시(UTC)';
COMMENT ON COLUMN reservation_orders.cancelled_at IS '예약 주문 취소 일시(UTC)';

COMMENT ON TABLE reservation_order_items IS '예약 주문에 포함된 상품별 상세 항목';
COMMENT ON COLUMN reservation_order_items.id IS '예약 주문 항목 식별자';
COMMENT ON COLUMN reservation_order_items.order_id IS '소속 예약 주문 식별자';
COMMENT ON COLUMN reservation_order_items.product_id IS '주문 시점의 상품 식별자';
COMMENT ON COLUMN reservation_order_items.product_name IS '주문 시점의 상품명 스냅샷';
COMMENT ON COLUMN reservation_order_items.unit_price IS '주문 시점의 상품 단가 스냅샷';
COMMENT ON COLUMN reservation_order_items.quantity IS '주문 수량';
