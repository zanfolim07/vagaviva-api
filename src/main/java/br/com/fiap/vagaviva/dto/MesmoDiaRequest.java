package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotNull;

public class MesmoDiaRequest {

    @NotNull(message = "Informe true ou false.")
    private Boolean aceita;

    public Boolean getAceita() {
        return aceita;
    }

    public void setAceita(Boolean aceita) {
        this.aceita = aceita;
    }
}
