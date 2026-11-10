package br.com.fiap.vagaviva.dto;

import java.util.List;

public class RiscoResponse {

    private Double valor;
    private String nivel;
    private List<String> motivos;

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
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
