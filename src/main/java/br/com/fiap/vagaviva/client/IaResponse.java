package br.com.fiap.vagaviva.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IaResponse {

    @JsonProperty("probabilidade_falta")
    private Double probabilidadeFalta;

    @JsonProperty("nivel")
    private String nivel;

    @JsonProperty("motivos")
    private List<String> motivos;

    public Double getProbabilidadeFalta() {
        return probabilidadeFalta;
    }

    public void setProbabilidadeFalta(Double probabilidadeFalta) {
        this.probabilidadeFalta = probabilidadeFalta;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public List<String> getMotivos() {
        return motivos;
    }

    public void setMotivos(List<String> motivos) {
        this.motivos = motivos;
    }
}
