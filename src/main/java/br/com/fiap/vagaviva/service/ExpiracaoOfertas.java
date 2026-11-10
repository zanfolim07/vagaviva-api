package br.com.fiap.vagaviva.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class ExpiracaoOfertas {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoOfertas.class);

    private final ListaEsperaService service;

    public ExpiracaoOfertas(ListaEsperaService service) {
        this.service = service;
    }

    @Scheduled(initialDelayString = "${oferta.verificacao-ms}", fixedDelayString = "${oferta.verificacao-ms}")
    public void verificar() {
        try {
            int expiradas = service.expirarOfertas();
            if (expiradas > 0) {
                log.info("{} oferta(s) sem resposta passaram ao próximo da fila.", expiradas);
            }
        } catch (SQLException e) {
            log.warn("Não foi possível conferir as ofertas vencidas (ORA-{}).", e.getErrorCode());
        }
    }
}
