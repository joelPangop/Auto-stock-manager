package org.autostock.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.autostock.enums.CategorieDepense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DepenseDto {
    private Long id;

    private Long voitureId;
    private Long entretienId; // nullable
    private Long documentId;  // nullable

    private CategorieDepense categorie;
    private BigDecimal montant;

    @JsonFormat(pattern = "yyyy-MM-dd['T'HH:mm[:ss]]")
    private LocalDateTime dateDepense;

    private String description;
    private Long fournisseurId;


}
