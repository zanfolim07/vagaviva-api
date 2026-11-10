package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.dao.ConsultaDao;
import br.com.fiap.vagaviva.dao.ListaEsperaDao;
import br.com.fiap.vagaviva.dto.PainelResponse;
import br.com.fiap.vagaviva.model.Consulta;
import br.com.fiap.vagaviva.model.ListaEspera;
import br.com.fiap.vagaviva.model.StatusConsulta;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class PainelService {

    public static final double VALOR_POR_CONSULTA = 13.85;

    private static final int DIAS_ANTES_PADRAO = 90;
    private static final int DIAS_DEPOIS_PADRAO = 30;

    private final ConsultaDao consultaDao;
    private final ListaEsperaDao listaEsperaDao;
    private final UnidadeService unidadeService;

    public PainelService(ConsultaDao consultaDao, ListaEsperaDao listaEsperaDao, UnidadeService unidadeService) {
        this.consultaDao = consultaDao;
        this.listaEsperaDao = listaEsperaDao;
        this.unidadeService = unidadeService;
    }

    public PainelResponse gerar(Long unidadeId, LocalDate inicio, LocalDate fim) throws SQLException {
        if (unidadeId != null) {
            unidadeService.obter(unidadeId);
        }
        LocalDate hoje = Relogio.hoje();
        LocalDate de = inicio != null ? inicio : (fim != null ? fim : hoje).minusDays(DIAS_ANTES_PADRAO);
        LocalDate ate = fim != null ? fim : hoje.plusDays(DIAS_DEPOIS_PADRAO);
        PainelResponse r = new PainelResponse();
        r.setUnidadeId(unidadeId);

        int[] encerradas = consultaDao.contarEncerradas(de, ate, unidadeId);
        r.setConsultas(encerradas[0]);
        r.setFaltas(encerradas[1]);
        r.setTaxaFalta(encerradas[0] == 0 ? 0.0
                : Math.round(encerradas[1] * 1000.0 / encerradas[0]) / 1000.0);

        int recuperadas = listaEsperaDao.contarVagasRecuperadas(de, ate, unidadeId);
        r.setVagasRecuperadas(recuperadas);
        r.setVagasDevolvidas(consultaDao.contarVagasDevolvidas(de, ate, unidadeId) + recuperadas);
        r.setValorPorConsulta(VALOR_POR_CONSULTA);
        r.setValorPreservado(Math.round(recuperadas * VALOR_POR_CONSULTA * 100) / 100.0);
        r.setPorSemana(consultaDao.recuperadasPorSemana(de, ate, unidadeId));
        r.setPorEspecialidade(consultaDao.taxaFaltaPorEspecialidade(de, ate, unidadeId));

        List<ListaEspera> fila = listaEsperaDao.listar(null, unidadeId, null, null).stream()
                .filter(ListaEspera::naFila)
                .toList();
        r.setPacientesNaFila(fila.size());
        r.setOfertasAbertas((int) fila.stream().filter(e -> ListaEspera.OFERTADA.equals(e.getStatus())).count());
        r.setPrazosVencidos((int) fila.stream().filter(e -> e.prazoVencido(hoje)).count());

        List<Consulta> consultasHoje = consultaDao.listar(hoje, unidadeId, null, null, null);
        r.setConsultasHoje(consultasHoje.size());
        r.setRiscoAltoHoje((int) consultasHoje.stream()
                .filter(c -> "ALTO".equals(c.getNivelRisco()) && c.getStatus() == StatusConsulta.AGENDADA)
                .count());
        return r;
    }
}
