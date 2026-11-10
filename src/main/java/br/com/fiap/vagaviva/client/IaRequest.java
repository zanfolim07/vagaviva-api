package br.com.fiap.vagaviva.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IaRequest {

    @JsonProperty("idade")
    private Integer idade;

    @JsonProperty("dias_antecedencia")
    private Long diasAntecedencia;

    @JsonProperty("mesmo_dia")
    private Boolean mesmoDia;

    @JsonProperty("dia_semana")
    private Integer diaSemana;

    @JsonProperty("recebeu_lembrete")
    private Boolean recebeuLembrete;

    @JsonProperty("n_consultas_anteriores")
    private Integer consultasAnteriores;

    @JsonProperty("faltas_anteriores")
    private Integer faltasAnteriores;

    @JsonProperty("taxa_faltas_anterior")
    private Double taxaFaltasAnterior;

    @JsonProperty("primeira_consulta")
    private Boolean primeiraConsulta;

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Long getDiasAntecedencia() {
        return diasAntecedencia;
    }

    public void setDiasAntecedencia(Long diasAntecedencia) {
        this.diasAntecedencia = diasAntecedencia;
    }

    public Boolean getMesmoDia() {
        return mesmoDia;
    }

    public void setMesmoDia(Boolean mesmoDia) {
        this.mesmoDia = mesmoDia;
    }

    public Integer getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(Integer diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Boolean getRecebeuLembrete() {
        return recebeuLembrete;
    }

    public void setRecebeuLembrete(Boolean recebeuLembrete) {
        this.recebeuLembrete = recebeuLembrete;
    }

    public Integer getConsultasAnteriores() {
        return consultasAnteriores;
    }

    public void setConsultasAnteriores(Integer consultasAnteriores) {
        this.consultasAnteriores = consultasAnteriores;
    }

    public Integer getFaltasAnteriores() {
        return faltasAnteriores;
    }

    public void setFaltasAnteriores(Integer faltasAnteriores) {
        this.faltasAnteriores = faltasAnteriores;
    }

    public Double getTaxaFaltasAnterior() {
        return taxaFaltasAnterior;
    }

    public void setTaxaFaltasAnterior(Double taxaFaltasAnterior) {
        this.taxaFaltasAnterior = taxaFaltasAnterior;
    }

    public Boolean getPrimeiraConsulta() {
        return primeiraConsulta;
    }

    public void setPrimeiraConsulta(Boolean primeiraConsulta) {
        this.primeiraConsulta = primeiraConsulta;
    }
}
