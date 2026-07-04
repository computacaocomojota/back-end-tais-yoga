package com.taisyoga.backend.agendamento;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoDTO {

    private Long id;

    @NotBlank(message = "O título da aula é obrigatório.")
    private String tituloAula;

    @NotBlank(message = "A modalidade é obrigatória.")
    private String modalidade;

    @NotBlank(message = "A data da aula é obrigatória.")
    private String dataAula;

    @NotBlank(message = "O horário é obrigatório.")
    private String horario;

    @NotBlank(message = "O nome do instrutor é obrigatório.")
    private String instrutor;

    @NotBlank(message = "O status é obrigatório.")
    private String status;

    private String observacoes;

    private Long usuarioId;
    private String usuarioNome;

    public static AgendamentoDTO fromEntity(Agendamento agendamento) {
        return new AgendamentoDTO(
                agendamento.getId(),
                agendamento.getTituloAula(),
                agendamento.getModalidade(),
                agendamento.getDataAula(),
                agendamento.getHorario(),
                agendamento.getInstrutor(),
                agendamento.getStatus(),
                agendamento.getObservacoes(),
                agendamento.getUsuario() != null ? agendamento.getUsuario().getId() : null,
                agendamento.getUsuario() != null ? agendamento.getUsuario().getNome() : null
        );
    }

    public Agendamento toEntity() {
        return new Agendamento(
                this.id,
                this.tituloAula,
                this.modalidade,
                this.dataAula,
                this.horario,
                this.instrutor,
                this.status,
                this.observacoes,
                null
        );
    }
}
