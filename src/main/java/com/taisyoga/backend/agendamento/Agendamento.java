package com.taisyoga.backend.agendamento;

import com.taisyoga.backend.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo_aula", nullable = false, length = 150)
    private String tituloAula;

    @Column(nullable = false, length = 50)
    private String modalidade;

    @Column(name = "data_aula", nullable = false, length = 50)
    private String dataAula;

    @Column(nullable = false, length = 20)
    private String horario;

    @Column(nullable = false, length = 120)
    private String instrutor;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(length = 1000)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
