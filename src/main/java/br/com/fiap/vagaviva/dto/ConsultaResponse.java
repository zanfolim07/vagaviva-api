package br.com.fiap.vagaviva.dto;

import br.com.fiap.vagaviva.model.StatusConsulta;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConsultaResponse {

    private Long id;
    private String tokenPaciente;
    private PacienteConsultaResumo paciente;
    private ProfissionalConsultaResumo profissional;
    private UnidadeResumo unidade;
    private LocalDate dataAgendamento;
    private LocalDateTime dataConsulta;
    private StatusConsulta status;
    private boolean enviarLembrete;
    private RiscoResponse risco;
    private String acaoSugerida;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PacienteConsultaResumo getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteConsultaResumo paciente) {
        this.paciente = paciente;
    }

    public ProfissionalConsultaResumo getProfissional() {
        return profissional;
    }

    public void setProfissional(ProfissionalConsultaResumo profissional) {
        this.profissional = profissional;
    }

    public UnidadeResumo getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeResumo unidade) {
        this.unidade = unidade;
    }

    public LocalDate getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(LocalDate dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public boolean isEnviarLembrete() {
        return enviarLembrete;
    }

    public void setEnviarLembrete(boolean enviarLembrete) {
        this.enviarLembrete = enviarLembrete;
    }

    public RiscoResponse getRisco() {
        return risco;
    }

    public void setRisco(RiscoResponse risco) {
        this.risco = risco;
    }

    public String getAcaoSugerida() {
        return acaoSugerida;
    }

    public void setAcaoSugerida(String acaoSugerida) {
        this.acaoSugerida = acaoSugerida;
    }

    public String getTokenPaciente() {
        return tokenPaciente;
    }

    public void setTokenPaciente(String tokenPaciente) {
        this.tokenPaciente = tokenPaciente;
    }
}
