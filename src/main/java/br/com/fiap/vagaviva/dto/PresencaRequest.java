package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotNull;

public class PresencaRequest {

    @NotNull(message = "Informe se o paciente compareceu (true) ou faltou (false).")
    private Boolean compareceu;

    public Boolean getCompareceu() {
        return compareceu;
    }

    public void setCompareceu(Boolean compareceu) {
        this.compareceu = compareceu;
    }
}
