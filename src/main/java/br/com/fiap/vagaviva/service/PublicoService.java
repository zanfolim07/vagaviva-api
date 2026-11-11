package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.dao.ConsultaDao;
import br.com.fiap.vagaviva.dto.ConsultaPublicaResponse;
import br.com.fiap.vagaviva.dto.FilaPublicaResponse;
import br.com.fiap.vagaviva.dto.OfertaPublicaResponse;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.model.Consulta;
import br.com.fiap.vagaviva.model.ListaEspera;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PublicoService {

    private static final String LINK_INVALIDO = "Link inválido ou expirado.";
    private static final int DIAS_VALIDADE_LINK = 1;

    private final ConsultaDao consultaDao;
    private final ConsultaService consultaService;
    private final ListaEsperaService listaEsperaService;

    public PublicoService(ConsultaDao consultaDao, ConsultaService consultaService,
                          ListaEsperaService listaEsperaService) {
        this.consultaDao = consultaDao;
        this.consultaService = consultaService;
        this.listaEsperaService = listaEsperaService;
    }

    public ConsultaPublicaResponse consulta(String token) throws SQLException {
        return paraPublica(consultaPorToken(token));
    }

    public ConsultaPublicaResponse confirmar(String token) throws SQLException {
        Consulta c = consultaPorToken(token);
        consultaService.confirmar(c.getId());
        return consulta(token);
    }

    public ConsultaPublicaResponse cancelar(String token) throws SQLException {
        Consulta c = consultaPorToken(token);
        consultaService.cancelar(c.getId());
        return consulta(token);
    }

    public FilaPublicaResponse fila(String token) throws SQLException {
        ListaEspera e = pedidoPorToken(token);
        Integer posicao = listaEsperaService.posicaoNaFila(e);

        FilaPublicaResponse r = new FilaPublicaResponse();
        r.setPrimeiroNome(primeiroNome(e.getPaciente().getNome()));
        r.setEspecialidade(e.getEspecialidade().getNome());
        r.setUnidade(e.getUnidade().getNome());
        r.setPosicao(posicao);
        r.setTotalNaFila(listaEsperaService.tamanhoDaFila(e));
        LocalDate hoje = Relogio.hoje();
        Integer posicaoSemanaPassada = posicao == null
                ? null : listaEsperaService.posicaoRegistradaEmOuAntes(e.getId(), hoje.minusDays(7));
        r.setVariacaoSemana(posicaoSemanaPassada == null || posicao == null
                ? 0 : posicaoSemanaPassada - posicao);
        long diasEspera = e.diasEspera(Relogio.hoje());
        r.setDiasEspera(diasEspera);
        r.setPrazoAte(e.dataLimite());
        r.setPercentualPrazo((int) Math.round(diasEspera * 100.0 / e.prazoDias()));
        r.setEtapaAtual(etapa(e.getStatus()));
        r.setAceitaMesmoDia(e.isAceitaMesmoDia());
        if (ListaEspera.OFERTADA.equals(e.getStatus()) && e.getDataConsultaOfertada() != null) {
            OfertaPublicaResponse oferta = new OfertaPublicaResponse();
            oferta.setDataConsulta(e.getDataConsultaOfertada());
            oferta.setRespondaAte(e.getLimiteOferta());
            oferta.setProfissional(e.getProfissionalConsultaOfertada() == null
                    ? null : e.getProfissionalConsultaOfertada().getNome());
            oferta.setUnidade(e.getUnidade().getNome());
            r.setOferta(oferta);
        }
        if (posicao != null) {
            listaEsperaService.registrarPosicaoDoDia(e.getId(), hoje, posicao);
        }
        return r;
    }

    public FilaPublicaResponse aceitar(String token) throws SQLException {
        listaEsperaService.aceitar(pedidoPorToken(token).getId());
        return fila(token);
    }

    public FilaPublicaResponse recusar(String token) throws SQLException {
        listaEsperaService.recusar(pedidoPorToken(token).getId());
        return fila(token);
    }

    public FilaPublicaResponse mesmoDia(String token, boolean aceita) throws SQLException {
        listaEsperaService.alterarMesmoDia(pedidoPorToken(token).getId(), aceita);
        return fila(token);
    }

    public FilaPublicaResponse sair(String token) throws SQLException {
        listaEsperaService.desistir(pedidoPorToken(token).getId());
        return fila(token);
    }

    private Consulta consultaPorToken(String token) throws SQLException {
        return consultaDao.buscarPorToken(token)
                .filter(c -> linkValido(c.getDataConsulta()))
                .orElseThrow(() -> new RecursoNaoEncontradoException(LINK_INVALIDO));
    }

    private ListaEspera pedidoPorToken(String token) throws SQLException {
        return listaEsperaService.buscarPorToken(token)
                .filter(e -> !ListaEspera.ATENDIDA.equals(e.getStatus()) || linkValido(e.getDataConsultaOfertada()))
                .orElseThrow(() -> new RecursoNaoEncontradoException(LINK_INVALIDO));
    }

    private static boolean linkValido(LocalDateTime dataConsulta) {
        LocalDate ultimoDia = dataConsulta.toLocalDate().plusDays(DIAS_VALIDADE_LINK);
        return !Relogio.hoje().isAfter(ultimoDia);
    }

    private ConsultaPublicaResponse paraPublica(Consulta c) {
        ConsultaPublicaResponse r = new ConsultaPublicaResponse();
        r.setPrimeiroNome(primeiroNome(c.getPaciente().getNome()));
        r.setDataConsulta(c.getDataConsulta());
        r.setProfissional(c.getProfissional().getNome());
        r.setEspecialidade(c.getEspecialidade().getNome());
        r.setUnidade(c.getUnidade().getNome());
        r.setEndereco(c.getUnidade().getEndereco());
        r.setStatus(c.getStatus());
        return r;
    }

    private static String etapa(String status) {
        return switch (status) {
            case ListaEspera.OFERTADA -> "VAGA_OFERECIDA";
            case ListaEspera.ATENDIDA -> "CONSULTA_MARCADA";
            case ListaEspera.DESISTIU -> "SAIU_DA_FILA";
            default -> "NA_FILA";
        };
    }

    private static String primeiroNome(String nome) {
        return nome == null ? null : nome.trim().split("\\s+")[0];
    }
}
