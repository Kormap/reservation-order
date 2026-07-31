ALTER TABLE reservation_orders
    ADD COLUMN delivery_address VARCHAR(255);

COMMENT ON COLUMN reservation_orders.delivery_address IS '주문 배송지 주소';
