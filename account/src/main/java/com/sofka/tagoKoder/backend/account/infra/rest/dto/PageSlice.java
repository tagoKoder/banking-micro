package com.sofka.tagoKoder.backend.account.infra.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PageSlice<T> {
  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean last;

    public static <T> PageSlice<T> of(List<T> content, int page, int size,
                                       long totalElements, int totalPages, boolean last) {
    return PageSlice.<T>builder()
        .content(content).page(page).size(size)
        .totalElements(totalElements).totalPages(totalPages).last(last)
        .build();
  }
}

