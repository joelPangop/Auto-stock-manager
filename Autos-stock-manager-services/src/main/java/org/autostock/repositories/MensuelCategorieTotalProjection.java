package org.autostock.repositories;

import java.math.BigDecimal;

public interface MensuelCategorieTotalProjection {
    Integer getYear();
    Integer getMonth();
    String getCategorie();   // on récupère la valeur string (ENUM)
    BigDecimal getTotal();
}
