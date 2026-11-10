package br.com.fiap.vagaviva.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ListaEspera {

    public static final String AGUARDANDO = "AGUARDANDO";
    public static final String OFERTADA = "OFERTADA";
    public static final String ATENDIDA = "ATENDIDA";
    public static final String DESISTIU = "DESISTIU";

    private Long id;
    private Paciente paciente;
    private Especialidade especialidade;
    private Unidade unidade;
    private Profissional solicitante;
    private Profissional profissionalPreferido;
    private Long consultaOfertadaId;
    private LocalDateTime dataConsultaOfertada;
    private Profissional profissionalConsultaOfertada;
    private LocalDate dataEntrada;
    private String classeRisco;
    private String justificativaClinica;
    private boolean prazoLegal;
    private String prioridadeLegal;
    private boolean aceitaMesmoDia;
    private String token;
    private Integer ultimaPosicao;
    private String status;
    private LocalDateTime limiteOferta;

    public int prazoDias() {
        return switch (classeRisco) {
            case "P1" -> 30;
            case "P2" -> 90;
            case "P3" -> 120;
            default -> 180;
        };
    }

    public int pesoClasse() {
        return switch (classeRisco) {
            case "P1" -> 100;
            case "P2" -> 50;
            case "P3" -> 20;
            default -> 0;
        };
    }

    public long diasEspera(LocalDate hoje) {
        return ChronoUnit.DAYS.between(dataEntrada, hoje);
    }

    public long pontuacao(LocalDate hoje) {
        return pesoClasse() + Math.round(diasEspera(hoje) * 100.0 / prazoDias());
    }

    public LocalDate dataLimite() {
        return dataEntrada.plusDays(prazoDias());
    }

    public boolean prazoVencido(LocalDate hoje) {
        return diasEspera(hoje) > prazoDias();
    }

    public int ordemPrioridadeLegal() {
        if (prioridadeLegal == null) {
            return 4;
        }
        return switch (prioridadeLegal) {
            case "80+" -> 0;
            case "60+" -> 1;
            case "GESTANTE" -> 2;
            case "PCD" -> 3;
            default -> 4;
        };
    }

    public boolean ofertaVencida(LocalDateTime agora) {
        return limiteOferta != null && !limiteOferta.isAfter(agora);
    }

    public boolean naFila() {
        return AGUARDANDO.equals(status) || OFERTADA.equals(status);
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

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public Profissional getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Profissional solicitante) {
        this.solicitante = solicitante;
    }

    public Profissional getProfissionalPreferido() {
        return profissionalPreferido;
    }

    public void setProfissionalPreferido(Profissional profissionalPreferido) {
        this.profissionalPreferido = profissionalPreferido;
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

    public Profissional getProfissionalConsultaOfertada() {
        return profissionalConsultaOfertada;
    }

    public void setProfissionalConsultaOfertada(Profissional profissionalConsultaOfertada) {
        this.profissionalConsultaOfertada = profissionalConsultaOfertada;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUltimaPosicao() {
        return ultimaPosicao;
    }

    public void setUltimaPosicao(Integer ultimaPosicao) {
        this.ultimaPosicao = ultimaPosicao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLimiteOferta() {
        return limiteOferta;
    }

    public void setLimiteOferta(LocalDateTime limiteOferta) {
        this.limiteOferta = limiteOferta;
    }
}
