package org.autostock.mappers;

import org.autostock.dtos.ClientCreateDto;
import org.autostock.dtos.ClientDto;
import org.autostock.models.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public Client toEntity(ClientCreateDto dto) {
        Client c = new Client();
        c.setNom(dto.getNom());
        c.setEmail(dto.getEmail());
        c.setTelephone(dto.getTelephone());
        c.setAdresse(dto.getAdresse());
        return c;
    }

    public ClientDto toDto(Client c) {
        ClientDto dto = new ClientDto();
        dto.setId(c.getId()); // <-- id générique
        dto.setNom(c.getNom());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setAdresse(c.getAdresse());
        return dto;
    }
}
