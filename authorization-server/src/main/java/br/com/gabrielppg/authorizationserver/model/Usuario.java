package br.com.gabrielppg.authorizationserver.model;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Usuario {
    @Id
    @Column
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @Column
    private String login;

    @Column
    private String senha;

    @Column
    private String email;

    @Column(name = "google_sub", unique = true, length = 255)
    private String googleSub;

    @Type(ListArrayType.class)
    @Column (name="roles", columnDefinition = "varchar[]")
    private List<String> roles;



}
