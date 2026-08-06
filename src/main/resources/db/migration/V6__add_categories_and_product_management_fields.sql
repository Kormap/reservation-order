CREATE TABLE categories (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO categories (code, name) VALUES
    ('APPLIANCE', '가전'),
    ('GENERAL_MERCHANDISE', '잡화'),
    ('STATIONERY', '문구'),
    ('BOOK', '도서'),
    ('FASHION', '패션'),
    ('FOOD', '식품'),
    ('BEAUTY', '뷰티'),
    ('SPORTS_LEISURE', '스포츠/레저'),
    ('DIGITAL', '디지털');

ALTER TABLE products
    ADD COLUMN description VARCHAR(2000),
    ADD COLUMN category_code VARCHAR(50),
    ADD COLUMN updated_at TIMESTAMPTZ;

UPDATE products
SET category_code = 'GENERAL_MERCHANDISE',
    updated_at = created_at;

ALTER TABLE products
    ALTER COLUMN category_code SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL,
    ADD CONSTRAINT fk_products_category_code FOREIGN KEY (category_code) REFERENCES categories (code);

COMMENT ON TABLE categories IS '상품 대분류 카테고리 마스터';
COMMENT ON COLUMN products.description IS '상품 상세 설명';
COMMENT ON COLUMN products.category_code IS '상품 카테고리 코드';
COMMENT ON COLUMN products.updated_at IS '상품 정보 최종 수정 일시';
