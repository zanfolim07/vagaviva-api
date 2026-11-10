package br.com.fiap.vagaviva.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrazoOfertaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Test
    void vagaEmMenosDe24hTem30Minutos() {
        assertEquals(AGORA.plusMinutes(30), PrazoOferta.limite(AGORA, AGORA.plusHours(5)));
    }

    @Test
    void vagaQuaseNaHoraUsaMetadeDoTempo() {
        assertEquals(AGORA.plusMinutes(20), PrazoOferta.limite(AGORA, AGORA.plusMinutes(40)));
    }

    @Test
    void vagaEmDoisDiasTemQuatroHoras() {
        assertEquals(AGORA.plusHours(4), PrazoOferta.limite(AGORA, AGORA.plusDays(2)));
    }

    @Test
    void vagaEmCincoDiasVaiAte18hQuatroDiasAntes() {
        assertEquals(LocalDateTime.of(2026, 11, 11, 18, 0),
                PrazoOferta.limite(AGORA, LocalDateTime.of(2026, 11, 15, 9, 0)));
    }

    @Test
    void vagaDistanteNuncaPassaDe48Horas() {
        assertEquals(AGORA.plusHours(48), PrazoOferta.limite(AGORA, AGORA.plusDays(30)));
    }
}
