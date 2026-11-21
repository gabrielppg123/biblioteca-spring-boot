package br.com.gabrielppg.bibliotecaweb.dto;

import java.util.List;

public record PaginaLivros(
        List<LivroDTO> content,
        int number,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last
) {}
