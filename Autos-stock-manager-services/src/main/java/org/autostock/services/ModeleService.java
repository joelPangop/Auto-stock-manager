package org.autostock.services;

import org.autostock.dtos.ModeleCreateDto;
import org.autostock.dtos.ModeleDto;
import org.autostock.models.Modele;

import java.util.List;

public interface ModeleService extends IService<Modele, Long> {
    List<ModeleDto> listByMarque(Long idMarque);
    ModeleDto create(ModeleCreateDto dto);
}
