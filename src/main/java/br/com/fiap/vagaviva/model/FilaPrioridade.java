package br.com.fiap.vagaviva.model;

import java.time.LocalDate;
import java.util.Comparator;

public final class FilaPrioridade {

    private FilaPrioridade() {
    }

    public static Comparator<ListaEspera> ordem(LocalDate hoje) {
        Comparator<ListaEspera> prazoLegalPrimeiro =
                Comparator.comparing((ListaEspera e) -> !e.isPrazoLegal());
        Comparator<ListaEspera> prazoMaisProximo =
                Comparator.comparing((ListaEspera e) -> e.isPrazoLegal() ? e.dataLimite() : LocalDate.MAX);
        Comparator<ListaEspera> maiorPontuacao =
                Comparator.comparingLong((ListaEspera e) -> e.pontuacao(hoje)).reversed();

        return prazoLegalPrimeiro
                .thenComparing(prazoMaisProximo)
                .thenComparing(maiorPontuacao)
                .thenComparingInt(ListaEspera::ordemPrioridadeLegal)
                .thenComparing(ListaEspera::getDataEntrada)
                .thenComparing(ListaEspera::getId);
    }
}
