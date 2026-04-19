package com.esi.payment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void of_validValues_createsMoney() {
        Money money = Money.of(BigDecimal.valueOf(10.50), "EUR");
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(10.50));
        assertThat(money.currency()).isEqualTo("EUR");
    }

    @Test
    void of_zeroAmount_isValid() {
        Money money = Money.of(BigDecimal.ZERO, "USD");
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void of_negativeAmount_throwsIllegalArgument() {
        assertThatThrownBy(() -> Money.of(BigDecimal.valueOf(-1), "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_nullAmount_throwsIllegalArgument() {
        assertThatThrownBy(() -> Money.of(null, "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_blankCurrency_throwsIllegalArgument() {
        assertThatThrownBy(() -> Money.of(BigDecimal.TEN, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_sameCurrencyAndAmount_areEqual() {
        Money a = Money.of(BigDecimal.valueOf(20), "EUR");
        Money b = Money.of(BigDecimal.valueOf(20), "EUR");
        assertThat(a).isEqualTo(b);
    }
}
