package org.autostock.services;

import org.autostock.dtos.MarqueCreateDto;
import org.autostock.dtos.MarqueDto;
import org.autostock.models.Marque;

import java.util.List;

public interface MarqueService extends IService<Marque, Long> {

    List<MarqueDto> list();
    MarqueDto create(MarqueCreateDto dto);
}
