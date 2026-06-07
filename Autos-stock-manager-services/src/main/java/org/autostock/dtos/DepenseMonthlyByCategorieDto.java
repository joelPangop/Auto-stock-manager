package org.autostock.dtos;

import java.math.BigDecimal;

public record DepenseMonthlyByCategorieDto (int year, int month, String categorie, BigDecimal total){
}
