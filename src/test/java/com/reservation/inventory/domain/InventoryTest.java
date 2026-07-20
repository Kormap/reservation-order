package com.reservation.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.reservation.common.exception.BusinessException;
import com.reservation.common.exception.ErrorCode;
import com.reservation.product.domain.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InventoryTest {

    @Test
    void 재고를_차감하고_복구한다() {
        Inventory inventory = new Inventory(new Product("상품", BigDecimal.TEN), 10);

        inventory.decrease(4);
        inventory.increase(4);

        assertThat(inventory.getQuantity()).isEqualTo(10);
    }

    @Test
    void 보유_수량보다_많이_차감하면_실패한다() {
        Inventory inventory = new Inventory(new Product("상품", BigDecimal.TEN), 3);

        assertThatThrownBy(() -> inventory.decrease(4))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
        assertThat(inventory.getQuantity()).isEqualTo(3);
    }

    @Test
    void 음수_재고는_생성할_수_없다() {
        Product product = new Product("상품", BigDecimal.TEN);

        assertThatThrownBy(() -> new Inventory(product, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
