package br.com.fiap.vagaviva.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ListaEsperaResponse {

    private Long id;
    private String tokenPaciente;
    private Integer posicao;
    private PacienteResumo paciente;
    private EspecialidadeResponse especialidade;
    private UnidadeResumo unidade;
    private ProfissionalResumo solicitante;
    private ProfissionalResumo profissionalPreferido;
    private LocalDate dataEntrada;
    private Long diasEspera;
    private Integer prazoDias;
    private LocalDate dataLimite;
    private boolean prazoVencido;
    private Long pontuacao;
    private String classeRisco;
    private boolean prazoLegal;
    private String prioridadeLegal;
    private boolean aceitaMesmoDia;
    private String status;
    private Long consultaOfertadaId;
    private LocalDateTime dataConsultaOfertada;
    private LocalDateTime respondaAte;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String justificativaClinica;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPosicao() {
        return posicao;
    }

    public void setPosicao(Integer posicao) {
        this.posicao = posicao;
    }

    public PacienteResumo getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteResumo paciente) {
        this.paciente = paciente;
    }

    public EspecialidadeResponse getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(EspecialidadeResponse especialidade) {
        this.especialidade = especialidade;
    }

    public UnidadeResumo getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeResumo unidade) {
        this.unidade = unidade;
    }

    public ProfissionalResumo getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(ProfissionalResumo solicitante) {
        this.solicitante = solicitante;
    }

    public ProfissionalResumo getProfissionalPreferido() {
        return profissionalPreferido;
    }

    public void setProfissionalPreferido(ProfissionalResumo profissionalPreferido) {
        this.profissionalPreferido = profissionalPreferido;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public Long getDiasEspera() {
        return diasEspera;
    }

    public void setDiasEspera(Long diasEspera) {
        this.diasEspera = diasEspera;
    }

    public Integer getPrazoDias() {
        return prazoDias;
    }

    public void setPrazoDias(Integer prazoDias) {
        this.prazoDias = prazoDias;
    }

    public LocalDate getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
    }

    public boolean isPrazoVencido() {
        return prazoVencido;
    }

    public void setPrazoVencido(boolean prazoVencido) {
        this.prazoVencido = prazoVencido;
    }

    public Long getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(Long pontuacao) {
        this.pontuacao = pontuacao;
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

    public String getPrioridadeLegal() {
        return prioridadeLegal;
    }

    public void setPrioridadeLegal(String prioridadeLegal) {
        this.prioridadeLegal = prioridadeLegal;
    }

    public boolean isAceitaMesmoDia() {
        return aceitaMesmoDia;
    }

    public void setAceitaMesmoDia(boolean aceitaMesmoDia) {
        this.aceitaMesmoDia = aceitaMesmoDia;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getConsultaOfertadaId() {
        return consultaOfertadaId;
    }

    public void setConsultaOfertadaId(Long consultaOfertadaId) {
        this.consultaOfertadaId = consultaOfertadaId;
    }

    public LocalDateTime getDataConsultaOfertada() {
        return dataConsultaOfertada;
    }

    public void setDataConsultaOfertada(LocalDateTime dataConsultaOfertada) {
        this.dataConsultaOfertada = dataConsultaOfertada;
    }

    public String getJustificativaClinica() {
        return justificativaClinica;
    }

    public void setJustificativaClinica(String justificativaClinica) {
        this.justificativaClinica = justificativaClinica;
    }

    public LocalDateTime getRespondaAte() {
        return respondaAte;
    }

    public void setRespondaAte(LocalDateTime respondaAte) {
        this.respondaAte = respondaAte;
    }

    public String getTokenPaciente() {
        return tokenPaciente;
    }

    public void setTokenPaciente(String tokenPaciente) {
        this.tokenPaciente = tokenPaciente;
    }
}
