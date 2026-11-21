package br.com.gabrielppg.biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "autor", schema = "public")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Autor {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @Column(name="nome", length = 100, nullable = false)
    private String nome;

    @Column(name="data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name="nacionalidade", length = 50, nullable = false)
    private String nacionalidade;

    @OneToMany(mappedBy = "autor")
    private List<Livro> livros;

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
