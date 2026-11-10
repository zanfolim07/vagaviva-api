package br.com.fiap.vagaviva.model;

public enum StatusConsulta {
    AGENDADA,
    CONFIRMADA,
    REALIZADA,
    FALTA,
    CANCELADA,
    LIBERADA;

    public boolean emAberto() {
        return this == AGENDADA || this == CONFIRMADA;
    }

    public boolean vagaDevolvida() {
        return this == CANCELADA || this == LIBERADA;
    }
}
