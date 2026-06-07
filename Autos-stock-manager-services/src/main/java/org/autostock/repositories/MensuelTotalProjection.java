package org.autostock.repositories;

import java.math.BigDecimal;

public interface MensuelTotalProjection {
    Integer getYear();
    Integer getMonth();
    BigDecimal getTotal();
}
