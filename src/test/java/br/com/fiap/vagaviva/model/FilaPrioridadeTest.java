package br.com.fiap.vagaviva.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilaPrioridadeTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 19);

    private static ListaEspera pedido(long id, String classe, boolean prazoLegal, String prioridade, int diasNaFila) {
        ListaEspera e = new ListaEspera();
        e.setId(id);
        e.setClasseRisco(classe);
        e.setPrazoLegal(prazoLegal);
        e.setPrioridadeLegal(prioridade);
        e.setDataEntrada(HOJE.minusDays(diasNaFila));
        return e;
    }

    private static List<Long> ordem(ListaEspera... pedidos) {
        return Stream.of(pedidos).sorted(FilaPrioridade.ordem(HOJE)).map(ListaEspera::getId).toList();
    }

    @Test
    void prazoLegalVemAntesDeQualquerPontuacao() {
        assertEquals(List.of(2L, 1L), ordem(pedido(1, "P2", false, null, 200), pedido(2, "P1", true, null, 0)));
    }

    @Test
    void entreCasosComPrazoLegalVemOPrazoMaisProximo() {
        assertEquals(List.of(2L, 1L), ordem(pedido(1, "P1", true, null, 5), pedido(2, "P1", true, null, 20)));
    }

    @Test
    void maiorPontuacaoVemPrimeiro() {
        assertEquals(List.of(2L, 1L), ordem(pedido(1, "P4", false, null, 10), pedido(2, "P2", false, null, 10)));
    }

    @Test
    void empateUsaPrioridadeLegal() {
        assertEquals(List.of(3L, 2L, 1L), ordem(pedido(1, "P4", false, null, 0),
                pedido(2, "P4", false, "60+", 0), pedido(3, "P4", false, "80+", 0)));
    }
}
