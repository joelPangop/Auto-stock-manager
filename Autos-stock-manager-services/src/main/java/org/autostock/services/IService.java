package org.autostock.services;

import java.util.List;
import java.util.Optional;

public interface IService <T, ID>{
    
    T create(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);

}
