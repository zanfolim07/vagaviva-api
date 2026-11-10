package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ListaEsperaRequest {

    @NotNull(message = "Informe o paciente.")
    @Positive(message = "Informe um paciente válido.")
    private Long pacienteId;

    @NotNull(message = "Informe a especialidade.")
    @Positive(message = "Informe uma especialidade válida.")
    private Long especialidadeId;

    @NotNull(message = "Informe a unidade.")
    @Positive(message = "Informe uma unidade válida.")
    private Long unidadeId;

    @NotNull(message = "Informe o profissional que fez o pedido.")
    @Positive(message = "Informe um profissional válido.")
    private Long profissionalSolicitanteId;

    @Positive(message = "Informe um profissional válido.")
    private Long profissionalPreferidoId;

    @NotNull(message = "Informe a classe de risco.")
    @Pattern(regexp = "P1|P2|P3|P4", message = "A classe de risco deve ser P1, P2, P3 ou P4.")
    private String classeRisco;

    @NotBlank(message = "Informe a justificativa clínica.")
    @Size(min = 20, max = 500, message = "A justificativa deve ter entre 20 e 500 caracteres.")
    private String justificativaClinica;
    private Boolean prazoLegal;

    @Pattern(regexp = "60\\+|80\\+|GESTANTE|PCD", message = "A prioridade legal deve ser 60+, 80+, GESTANTE ou PCD.")
    private String prioridadeLegal;
    private Boolean aceitaMesmoDia;

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getEspecialidadeId() {
        return especialidadeId;
    }

    public void setEspecialidadeId(Long especialidadeId) {
        this.especialidadeId = especialidadeId;
    }

    public Long getUnidadeId() {
        return unidadeId;
    }

    public void setUnidadeId(Long unidadeId) {
        this.unidadeId = unidadeId;
    }

    public Long getProfissionalSolicitanteId() {
        return profissionalSolicitanteId;
    }

    public void setProfissionalSolicitanteId(Long profissionalSolicitanteId) {
        this.profissionalSolicitanteId = profissionalSolicitanteId;
    }

    public Long getProfissionalPreferidoId() {
        return profissionalPreferidoId;
    }

    public void setProfissionalPreferidoId(Long profissionalPreferidoId) {
        this.profissionalPreferidoId = profissionalPreferidoId;
    }

    public String getClasseRisco() {
        return classeRisco;
    }

    public void setClasseRisco(String classeRisco) {
        this.classeRisco = classeRisco;
    }

    public String getJustificativaClinica() {
        return justificativaClinica;
    }

    public void setJustificativaClinica(String justificativaClinica) {
        this.justificativaClinica = justificativaClinica;
    }

    public Boolean getPrazoLegal() {
        return prazoLegal;
    }

    public void setPrazoLegal(Boolean prazoLegal) {
        this.prazoLegal = prazoLegal;
    }

    public String getPrioridadeLegal() {
        return prioridadeLegal;
    }

    public void setPrioridadeLegal(String prioridadeLegal) {
        this.prioridadeLegal = prioridadeLegal;
    }

    public Boolean getAceitaMesmoDia() {
        return aceitaMesmoDia;
    }

    public void setAceitaMesmoDia(Boolean aceitaMesmoDia) {
        this.aceitaMesmoDia = aceitaMesmoDia;
    }
}
