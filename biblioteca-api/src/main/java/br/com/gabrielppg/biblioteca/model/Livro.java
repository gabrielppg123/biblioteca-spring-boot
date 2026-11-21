package br.com.gabrielppg.biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="livro")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Livro {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="isbn", length = 20, nullable = false)
    private String isbn;

    @Column(name="titulo", length = 150, nullable = false)
    private String titulo;

    @Column(name="data_publicacao")
    private LocalDate dataPublicacao;

    @Enumerated(EnumType.STRING)
    @Column(name="genero", length = 30, nullable = false)
    private GeneroLivro genero;

    @Column(name="preco", precision = 18, scale = 2)
    private BigDecimal preco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor", nullable = false)
    private Autor autor;

    @CreatedDate
    @Column(name="data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name="data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    // Referência à identidade emitida pelo Authorization Server; sem JOIN com usuario.
    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

}
