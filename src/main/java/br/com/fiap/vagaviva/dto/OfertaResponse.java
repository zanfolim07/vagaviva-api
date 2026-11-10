package br.com.fiap.vagaviva.dto;

import java.time.LocalDateTime;

public class OfertaResponse {

    private Long esperaId;
    private PacienteResumo paciente;
    private String classeRisco;
    private boolean prazoLegal;
    private Long pontuacao;
    private Long consultaId;
    private LocalDateTime dataConsulta;
    private LocalDateTime respondaAte;

    public Long getEsperaId() {
        return esperaId;
    }

    public void setEsperaId(Long esperaId) {
        this.esperaId = esperaId;
    }

    public PacienteResumo getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteResumo paciente) {
        this.paciente = paciente;
    }

    public String getClasseRisco() {
        return classeRisco;
    }

    public void setClasseRisco(String classeRisco) {
        this.classeRisco = classeRisco;
    }

    public boolean isPrazoLegal() {
        return prazoLegal;
    }

    public void setPrazoLegal(boolean prazoLegal) {
        this.prazoLegal = prazoLegal;
    }

    public Long getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(Long pontuacao) {
        this.pontuacao = pontuacao;
    }

    public Long getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }

    public LocalDateTime getRespondaAte() {
        return respondaAte;
    }

    public void setRespondaAte(LocalDateTime respondaAte) {
        this.respondaAte = respondaAte;
    }
}
