package br.com.gabrielppg.bibliotecaweb.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LivroDTO(
        UUID id,
        String isbn,
        String titulo,
        LocalDate dataPublicacao,
        String genero,
        BigDecimal preco,
        AutorDTO autor
) {}
