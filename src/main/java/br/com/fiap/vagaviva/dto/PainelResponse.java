package br.com.fiap.vagaviva.dto;

import java.util.List;

public class PainelResponse {

    private Integer consultas;
    private Integer faltas;
    private Double taxaFalta;
    private Integer vagasRecuperadas;
    private Double valorPreservado;
    private List<SemanaRecuperadasResponse> porSemana;
    private List<EspecialidadePainelResponse> porEspecialidade;
    private Long unidadeId;
    private Integer vagasDevolvidas;
    private Double valorPorConsulta;
    private Integer pacientesNaFila;
    private Integer ofertasAbertas;
    private Integer prazosVencidos;
    private Integer consultasHoje;
    private Integer riscoAltoHoje;

    public Integer getConsultas() { return consultas; }
    public void setConsultas(Integer consultas) { this.consultas = consultas; }
    public Integer getFaltas() { return faltas; }
    public void setFaltas(Integer faltas) { this.faltas = faltas; }
    public Double getTaxaFalta() { return taxaFalta; }
    public void setTaxaFalta(Double taxaFalta) { this.taxaFalta = taxaFalta; }
    public Integer getVagasRecuperadas() { return vagasRecuperadas; }
    public void setVagasRecuperadas(Integer vagasRecuperadas) { this.vagasRecuperadas = vagasRecuperadas; }
    public Double getValorPreservado() { return valorPreservado; }
    public void setValorPreservado(Double valorPreservado) { this.valorPreservado = valorPreservado; }
    public List<SemanaRecuperadasResponse> getPorSemana() { return porSemana; }
    public void setPorSemana(List<SemanaRecuperadasResponse> porSemana) { this.porSemana = porSemana; }
    public List<EspecialidadePainelResponse> getPorEspecialidade() { return porEspecialidade; }
    public void setPorEspecialidade(List<EspecialidadePainelResponse> porEspecialidade) {
        this.porEspecialidade = porEspecialidade;
    }

    public Long getUnidadeId() {
        return unidadeId;
    }

    public void setUnidadeId(Long unidadeId) {
        this.unidadeId = unidadeId;
    }

    public Integer getVagasDevolvidas() {
        return vagasDevolvidas;
    }

    public void setVagasDevolvidas(Integer vagasDevolvidas) {
        this.vagasDevolvidas = vagasDevolvidas;
    }

    public Double getValorPorConsulta() {
        return valorPorConsulta;
    }

    public void setValorPorConsulta(Double valorPorConsulta) {
        this.valorPorConsulta = valorPorConsulta;
    }

    public Integer getPacientesNaFila() {
        return pacientesNaFila;
    }

    public void setPacientesNaFila(Integer pacientesNaFila) {
        this.pacientesNaFila = pacientesNaFila;
    }

    public Integer getOfertasAbertas() {
        return ofertasAbertas;
    }

    public void setOfertasAbertas(Integer ofertasAbertas) {
        this.ofertasAbertas = ofertasAbertas;
    }

    public Integer getPrazosVencidos() {
        return prazosVencidos;
    }

    public void setPrazosVencidos(Integer prazosVencidos) {
        this.prazosVencidos = prazosVencidos;
    }

    public Integer getConsultasHoje() {
        return consultasHoje;
    }

    public void setConsultasHoje(Integer consultasHoje) {
        this.consultasHoje = consultasHoje;
    }

    public Integer getRiscoAltoHoje() {
        return riscoAltoHoje;
    }

    public void setRiscoAltoHoje(Integer riscoAltoHoje) {
        this.riscoAltoHoje = riscoAltoHoje;
    }
}
