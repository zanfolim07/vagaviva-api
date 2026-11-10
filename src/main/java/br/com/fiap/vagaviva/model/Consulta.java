package br.com.fiap.vagaviva.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Consulta {

    public static final String NIVEL_NAO_CALCULADO = "NAO_CALCULADO";

    private Long id;
    private Paciente paciente;
    private Profissional profissional;
    private LocalDateTime dataAgendamento;
    private LocalDateTime dataConsulta;
    private StatusConsulta status;
    private Double riscoFalta;
    private String nivelRisco;
    private String motivoRisco;
    private boolean lembrete;
    private String token;
    private boolean primeiraConsulta;
    private Especialidade especialidade;

    public Unidade getUnidade() {
        return profissional == null ? null : profissional.getUnidade();
    }

    public boolean faltamMenosDe24h(LocalDateTime agora) {
        return Duration.between(agora, dataConsulta).toHours() < 24;
    }

    public boolean jaPassou(LocalDateTime agora) {
        return !dataConsulta.isAfter(agora);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public LocalDateTime getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(LocalDateTime dataAgendamento) {
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

    public Double getRiscoFalta() {
        return riscoFalta;
    }

    public void setRiscoFalta(Double riscoFalta) {
        this.riscoFalta = riscoFalta;
    }

    public String getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(String nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public String getMotivoRisco() {
        return motivoRisco;
    }

    public void setMotivoRisco(String motivoRisco) {
        this.motivoRisco = motivoRisco;
    }

    public boolean isLembrete() {
        return lembrete;
    }

    public void setLembrete(boolean lembrete) {
        this.lembrete = lembrete;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isPrimeiraConsulta() {
        return primeiraConsulta;
    }

    public void setPrimeiraConsulta(boolean primeiraConsulta) {
        this.primeiraConsulta = primeiraConsulta;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }
}
