package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DepenseDashboardVm {
    private BigDecimal total;                // total global voiture
    private BigDecimal totalPeriode;         // total période si fournie
    private PageVm<DepenseDto> page;         // liste paginée
    private List<DepenseMonthlyTotalDto> monthly; // graph mensuel
    private List<DepenseMonthlyByCategorieDto> monthlyByCategorie; // graph par catégorie

}
