package br.com.fiap.vagaviva.dto;

import java.time.LocalDateTime;

public class OfertaPublicaResponse {

    private LocalDateTime dataConsulta;
    private LocalDateTime respondaAte;
    private String profissional;
    private String unidade;

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

    public String getProfissional() {
        return profissional;
    }

    public void setProfissional(String profissional) {
        this.profissional = profissional;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
}
