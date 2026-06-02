package org.autostock.dtos;

import java.math.BigDecimal;

public record DocumentUpdateDto(String type, String description, BigDecimal montant) {}
