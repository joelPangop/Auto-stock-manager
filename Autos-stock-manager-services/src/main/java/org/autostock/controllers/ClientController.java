package org.autostock.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.autostock.dtos.ClientCreateDto;
import org.autostock.dtos.ClientDto;
import org.autostock.mappers.ClientMapper;
import org.autostock.models.Client;
import org.autostock.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientMapper clientMapper;

    @PostMapping
    public ClientDto create(@RequestBody ClientCreateDto dto) {
        Client saved = clientService.create(clientMapper.toEntity(dto));
        return clientMapper.toDto(saved);
    }

    @GetMapping("/{id}")
    public ClientDto get(@PathVariable Long id) {
        Client c = clientService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable"));
        return clientMapper.toDto(c);
    }

    @GetMapping
    public List<ClientDto> list(@RequestParam(required = false) String nom) {
        List<Client> data = (nom == null || nom.isBlank())
                ? clientService.findAll()
                : clientService.rechercherParNom(nom);
        return data.stream().map(clientMapper::toDto).toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        clientService.deleteById(id);
    }

    @GetMapping("/count")
    public Integer count(){
        return clientService.findAll().size();
    }
}
