
package br.com.gabrielppg.biblioteca.controller.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record PaginaResposta<T>(
        List<T> content,
        int number,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last
) {
    public static <T> PaginaResposta<T> de(Page<T> pagina) {
        return new PaginaResposta<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getTotalPages(),
                pagina.getTotalElements(),
                pagina.isFirst(),
                pagina.isLast()
        );
    }
}
