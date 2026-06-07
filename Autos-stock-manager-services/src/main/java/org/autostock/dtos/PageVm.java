package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter @Setter
@Component
public class PageVm<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PageVm() {}

    public PageVm(List<T> items, int page, int size, long totalElements, int totalPages) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
    // getters/setters...
}
