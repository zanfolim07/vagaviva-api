package br.com.fiap.vagaviva.dto;

import java.time.LocalDate;

public class FilaPublicaResponse {

    private String primeiroNome;
    private String especialidade;
    private String unidade;
    private Integer posicao;
    private Integer totalNaFila;
    private Integer variacaoSemana;
    private Long diasEspera;
    private LocalDate prazoAte;
    private Integer percentualPrazo;
    private String etapaAtual;
    private boolean aceitaMesmoDia;
    private OfertaPublicaResponse oferta;

    public String getPrimeiroNome() {
        return primeiroNome;
    }

    public void setPrimeiroNome(String primeiroNome) {
        this.primeiroNome = primeiroNome;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public Integer getPosicao() {
        return posicao;
    }

    public void setPosicao(Integer posicao) {
        this.posicao = posicao;
    }

    public Integer getVariacaoSemana() {
        return variacaoSemana;
    }

    public void setVariacaoSemana(Integer variacaoSemana) {
        this.variacaoSemana = variacaoSemana;
    }

    public Integer getTotalNaFila() {
        return totalNaFila;
    }

    public void setTotalNaFila(Integer totalNaFila) {
        this.totalNaFila = totalNaFila;
    }

    public Long getDiasEspera() {
        return diasEspera;
    }

    public void setDiasEspera(Long diasEspera) {
        this.diasEspera = diasEspera;
    }

    public LocalDate getPrazoAte() {
        return prazoAte;
    }

    public void setPrazoAte(LocalDate prazoAte) {
        this.prazoAte = prazoAte;
    }

    public Integer getPercentualPrazo() {
        return percentualPrazo;
    }

    public void setPercentualPrazo(Integer percentualPrazo) {
        this.percentualPrazo = percentualPrazo;
    }

    public String getEtapaAtual() {
        return etapaAtual;
    }

    public void setEtapaAtual(String etapaAtual) {
        this.etapaAtual = etapaAtual;
    }

    public boolean isAceitaMesmoDia() {
        return aceitaMesmoDia;
    }

    public void setAceitaMesmoDia(boolean aceitaMesmoDia) {
        this.aceitaMesmoDia = aceitaMesmoDia;
    }

    public OfertaPublicaResponse getOferta() {
        return oferta;
    }

    public void setOferta(OfertaPublicaResponse oferta) {
        this.oferta = oferta;
    }
}
