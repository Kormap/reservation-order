ALTER TABLE reservation_orders
    ADD COLUMN contact_phone_number VARCHAR(20),
    ADD COLUMN recipient_name VARCHAR(100),
    ADD COLUMN delivery_request VARCHAR(500);

COMMENT ON COLUMN reservation_orders.contact_phone_number IS '연락처';
COMMENT ON COLUMN reservation_orders.recipient_name IS '수령인 이름';
COMMENT ON COLUMN reservation_orders.delivery_request IS '배송 요청사항';
