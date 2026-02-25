package com.innovatepam.idea.dto;

import org.springframework.data.domain.Page;

public record PageResponse<T>(
    java.util.List<T> content,
    PageableInfo pageable
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            new PageableInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
            )
        );
    }

    public record PageableInfo(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
    ) {}
}
