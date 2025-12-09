package org.autostock.services;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

public interface IService <T, ID>{
    
    T create(T entity) throws AccessDeniedException;

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);

}
