package org.autostock.dtos;

import java.math.BigDecimal;

public record DepenseMonthlyTotalDto (int year, int month, BigDecimal total){}
