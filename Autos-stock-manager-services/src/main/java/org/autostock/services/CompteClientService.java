package org.autostock.services;

import org.autostock.dtos.client.*;

public interface CompteClientService {
    ClientAuthResponse register(CompteClientRegisterDto dto);
    ClientAuthResponse login(CompteClientLoginDto dto);
    CompteClientDto getProfile(String email);
    CompteClientDto updateProfile(String email, CompteClientUpdateDto dto);
}
