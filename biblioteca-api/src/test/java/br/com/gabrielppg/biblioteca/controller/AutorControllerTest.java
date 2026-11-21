
package br.com.gabrielppg.biblioteca.controller;

import br.com.gabrielppg.biblioteca.controller.common.GlobalExceptionHandler;
import br.com.gabrielppg.biblioteca.controller.dto.AutorDTO;
import br.com.gabrielppg.biblioteca.controller.mappers.AutorMapper;
import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.service.AutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AutorControllerTest {

    @Mock
    private AutorService service;

    @Mock
    private AutorMapper mapper;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mvc = MockMvcBuilders
                .standaloneSetup(new AutorController(service, mapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void deveRetornar200QuandoAutorExiste() throws Exception {
        UUID id = UUID.randomUUID();

        Autor autor = new Autor();
        autor.setId(id);
        autor.setNome("Machado de Assis");

        AutorDTO dto = new AutorDTO(
                id,
                "Machado de Assis",
                LocalDate.of(1839, 6, 21),
                "Brasileiro"
        );

        when(service.obterAutorPeloId(id))
                .thenReturn(Optional.of(autor));

        when(mapper.toDTO(autor))
                .thenReturn(dto);

        mvc.perform(get("/autores/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Machado de Assis"))
                .andExpect(jsonPath("$.nacionalidade").value("Brasileiro"));

        verify(service).obterAutorPeloId(id);
        verify(mapper).toDTO(autor);
    }

    @Test
    void deveRetornar404QuandoAutorNaoExiste() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.obterAutorPeloId(id))
                .thenReturn(Optional.empty());

        mvc.perform(get("/autores/{id}", id))
                .andExpect(status().isNotFound());

        verify(service).obterAutorPeloId(id);
        verifyNoInteractions(mapper);
    }

    @Test
    void deveRetornar400QuandoUuidInvalido() throws Exception {
        mvc.perform(get("/autores/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service, mapper);
    }

    @Test
    void deveRetornar422QuandoDadosInvalidos() throws Exception {
        String json = """
                {
                    "nome": "",
                    "dataNascimento": "1990-01-01",
                    "nacionalidade": "Brasileiro"
                }
                """;

        mvc.perform(post("/autores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422));

        verifyNoInteractions(service, mapper);
    }
}
