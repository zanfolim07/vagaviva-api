package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.dao.ConsultaDao;
import br.com.fiap.vagaviva.dao.ListaEsperaDao;
import br.com.fiap.vagaviva.dao.ProfissionalDao;
import br.com.fiap.vagaviva.dto.ClasseRiscoResponse;
import br.com.fiap.vagaviva.dto.ConsultaResponse;
import br.com.fiap.vagaviva.dto.ListaEsperaRequest;
import br.com.fiap.vagaviva.dto.ListaEsperaResponse;
import br.com.fiap.vagaviva.dto.ListaEsperaUnidadeRequest;
import br.com.fiap.vagaviva.dto.RecusaResponse;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.exception.RegraNegocioException;
import br.com.fiap.vagaviva.model.Consulta;
import br.com.fiap.vagaviva.model.FilaPrioridade;
import br.com.fiap.vagaviva.model.ListaEspera;
import br.com.fiap.vagaviva.model.Profissional;
import br.com.fiap.vagaviva.model.StatusConsulta;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ListaEsperaService {

    private static final String RECUSADA = "RECUSADA";
    private static final String EXPIRADA = "EXPIRADA";

    private final ListaEsperaDao dao;
    private final ConsultaDao consultaDao;
    private final ProfissionalDao profissionalDao;
    private final PacienteService pacienteService;
    private final EspecialidadeService especialidadeService;
    private final UnidadeService unidadeService;
    private final ProfissionalService profissionalService;
    private final RiscoService riscoService;
    private final RealocacaoService realocacaoService;
    private final Transacao transacao;
    private final Conversor conversor;

    public ListaEsperaService(ListaEsperaDao dao, ConsultaDao consultaDao, ProfissionalDao profissionalDao,
                              PacienteService pacienteService, EspecialidadeService especialidadeService,
                              UnidadeService unidadeService, ProfissionalService profissionalService,
                              RiscoService riscoService, RealocacaoService realocacaoService, Transacao transacao,
                              Conversor conversor) {
        this.dao = dao;
        this.consultaDao = consultaDao;
        this.profissionalDao = profissionalDao;
        this.pacienteService = pacienteService;
        this.especialidadeService = especialidadeService;
        this.unidadeService = unidadeService;
        this.profissionalService = profissionalService;
        this.riscoService = riscoService;
        this.realocacaoService = realocacaoService;
        this.transacao = transacao;
        this.conversor = conversor;
    }

    public List<ListaEsperaResponse> listar(Long especialidadeId, Long unidadeId, Long pacienteId, String status)
            throws SQLException {
        List<ListaEspera> pedidos = dao.listar(especialidadeId, unidadeId, pacienteId, status);
        Map<String, Map<Long, Integer>> posicoesPorFila = new HashMap<>();
        Map<Long, Integer> posicao = new HashMap<>();
        for (ListaEspera e : pedidos) {
            if (e.naFila()) {
                String fila = e.getEspecialidade().getId() + "-" + e.getUnidade().getId();
                Map<Long, Integer> posicoes = posicoesPorFila.get(fila);
                if (posicoes == null) {
                    posicoes = posicoes(e.getEspecialidade().getId(), e.getUnidade().getId());
                    posicoesPorFila.put(fila, posicoes);
                }
                posicao.put(e.getId(), posicoes.get(e.getId()));
            }
        }
        pedidos.sort(Comparator
                .comparing((ListaEspera e) -> e.getEspecialidade().getNome())
                .thenComparing(e -> e.getUnidade().getNome())
                .thenComparing(e -> !e.naFila())
                .thenComparing(e -> posicao.getOrDefault(e.getId(), Integer.MAX_VALUE))
                .thenComparing(ListaEspera::getDataEntrada));
        return pedidos.stream().map(e -> conversor.listaEspera(e, posicao.get(e.getId()), false)).toList();
    }

    public List<ListaEsperaResponse> filaDaUnidade(Long unidadeId, Long especialidadeId) throws SQLException {
        unidadeService.obter(unidadeId);
        return listar(especialidadeId, unidadeId, null, null).stream()
                .filter(r -> r.getPosicao() != null)
                .toList();
    }

    public List<ListaEsperaResponse> listarDoPaciente(Long pacienteId) throws SQLException {
        pacienteService.obter(pacienteId);
        return listar(null, null, pacienteId, null);
    }

    public List<ClasseRiscoResponse> classes() {
        return List.of(classe("P1", "Alta"), classe("P2", "Média"), classe("P3", "Eletiva"), classe("P4", "Baixa"));
    }

    private static ClasseRiscoResponse classe(String classe, String nome) {
        ListaEspera e = new ListaEspera();
        e.setClasseRisco(classe);
        return new ClasseRiscoResponse(classe, nome, e.prazoDias(), e.pesoClasse());
    }

    public ListaEsperaResponse buscar(Long id) throws SQLException {
        ListaEspera e = obter(id);
        return conversor.listaEspera(e, posicaoNaFila(e), true);
    }

    public ListaEsperaResponse incluir(ListaEsperaRequest req) throws SQLException {
        ListaEspera e = paraModel(req, null);
        e.setDataEntrada(Relogio.hoje());
        e.setToken(ConsultaService.novoToken());
        e.setStatus(ListaEspera.AGUARDANDO);
        Long id = dao.inserir(e);
        ListaEspera salvo = obter(id);
        Integer posicao = posicaoNaFila(salvo);
        dao.atualizarUltimaPosicao(id, posicao);
        return conversor.listaEspera(salvo, posicao, true);
    }

    public ListaEsperaResponse incluir(Long unidadeId, ListaEsperaUnidadeRequest entrada) throws SQLException {
        ListaEsperaRequest req = new ListaEsperaRequest();
        req.setPacienteId(entrada.pacienteId());
        req.setEspecialidadeId(entrada.especialidadeId());
        req.setUnidadeId(unidadeId);
        req.setProfissionalSolicitanteId(entrada.profissionalSolicitanteId());
        req.setProfissionalPreferidoId(entrada.profissionalPreferidoId());
        req.setClasseRisco(entrada.classeRisco());
        req.setJustificativaClinica(entrada.justificativaClinica());
        req.setPrazoLegal(entrada.prazoLegal());
        req.setPrioridadeLegal(entrada.prioridadeLegal());
        req.setAceitaMesmoDia(entrada.aceitaMesmoDia());
        return incluir(req);
    }

    public ListaEsperaResponse atualizar(Long id, ListaEsperaRequest req) throws SQLException {
        ListaEspera atual = obter(id);
        if (!atual.naFila()) {
            throw new RegraNegocioException("O pedido está " + atual.getStatus() + " e não pode ser alterado.");
        }
        boolean mudouFila = !atual.getEspecialidade().getId().equals(req.getEspecialidadeId())
                || !atual.getUnidade().getId().equals(req.getUnidadeId())
                || !atual.getPaciente().getId().equals(req.getPacienteId());
        if (mudouFila && ListaEspera.OFERTADA.equals(atual.getStatus())) {
            throw new RegraNegocioException("O pedido tem uma vaga oferecida. Recuse a oferta antes de mudar o paciente, "
                    + "a especialidade ou a unidade.");
        }
        ListaEspera e = paraModel(req, id);
        e.setId(id);
        dao.atualizar(e);
        return buscar(id);
    }

    public void remover(Long id) throws SQLException {
        if (!dao.remover(id)) {
            throw naoEncontrado(id);
        }
    }

    public ConsultaResponse aceitar(Long id) throws SQLException {
        ListaEspera pedido = obter(id);
        int[] historico = riscoService.historico(pedido.getPaciente().getId());

        Long consultaId = transacao.executar(conn -> {
            dao.travar(conn, id);
            ListaEspera e = dao.buscarPorId(conn, id).orElseThrow(() -> naoEncontrado(id));
            if (!ListaEspera.OFERTADA.equals(e.getStatus()) || e.getConsultaOfertadaId() == null) {
                throw new RegraNegocioException("Não há vaga oferecida para este pedido.");
            }
            if (e.ofertaVencida(Relogio.agora())) {
                throw new RegraNegocioException("O prazo para responder a esta oferta terminou.");
            }
            consultaDao.travar(conn, e.getConsultaOfertadaId());
            Consulta vaga = consultaDao.buscarPorId(conn, e.getConsultaOfertadaId())
                    .orElseThrow(() -> new RegraNegocioException("A vaga oferecida não existe mais."));
            if (!vaga.getStatus().vagaDevolvida() || vaga.jaPassou(Relogio.agora())) {
                throw new RegraNegocioException("A vaga oferecida não está mais disponível.");
            }

            vaga.setPaciente(e.getPaciente());
            vaga.setEspecialidade(e.getEspecialidade());
            vaga.setStatus(StatusConsulta.CONFIRMADA);
            vaga.setDataAgendamento(Relogio.agora());
            vaga.setLembrete(true);
            vaga.setToken(ConsultaService.novoToken());
            riscoService.calcular(vaga, historico);
            consultaDao.atualizar(conn, vaga);
            dao.limparOfertas(conn, vaga.getId());

            dao.atualizarStatus(conn, id, ListaEspera.ATENDIDA, vaga.getId());
            return vaga.getId();
        });
        return conversor.consulta(consultaDao.buscarPorId(consultaId).orElseThrow(() -> naoEncontrado(id)));
    }

    public RecusaResponse recusar(Long id) throws SQLException {
        ListaEspera oferta = transacao.executar(conn -> devolverOferta(conn, id, ListaEspera.AGUARDANDO, RECUSADA));
        RecusaResponse resposta = new RecusaResponse();
        resposta.setEspera(buscar(id));
        resposta.setNovaOferta(conversor.oferta(oferta));
        return resposta;
    }

    public ListaEsperaResponse desistir(Long id) throws SQLException {
        transacao.executar(conn -> devolverOferta(conn, id, ListaEspera.DESISTIU, RECUSADA));
        return buscar(id);
    }

    public int expirarOfertas() throws SQLException {
        int expiradas = 0;
        for (Long id : dao.listarOfertasVencidas(Relogio.agora())) {
            boolean expirou = transacao.executar(conn -> {
                dao.travar(conn, id);
                ListaEspera e = dao.buscarPorId(conn, id).orElseThrow(() -> naoEncontrado(id));
                if (!ListaEspera.OFERTADA.equals(e.getStatus()) || !e.ofertaVencida(Relogio.agora())) {
                    return false;
                }
                devolverOferta(conn, id, ListaEspera.AGUARDANDO, EXPIRADA);
                return true;
            });
            if (expirou) {
                expiradas++;
            }
        }
        return expiradas;
    }

    public ListaEsperaResponse alterarMesmoDia(Long id, boolean aceita) throws SQLException {
        ListaEspera e = obter(id);
        if (!e.naFila()) {
            throw new RegraNegocioException("O pedido está " + e.getStatus() + " e não pode ser alterado.");
        }
        dao.atualizarMesmoDia(id, aceita);
        return buscar(id);
    }

    public Integer posicaoNaFila(ListaEspera e) throws SQLException {
        if (!e.naFila()) {
            return null;
        }
        return posicoes(e.getEspecialidade().getId(), e.getUnidade().getId()).get(e.getId());
    }

    public int tamanhoDaFila(ListaEspera e) throws SQLException {
        return dao.listarFila(e.getEspecialidade().getId(), e.getUnidade().getId()).size();
    }

    public void registrarPosicaoVista(Long id, Integer posicao) throws SQLException {
        dao.atualizarUltimaPosicao(id, posicao);
    }

    public ListaEspera obter(Long id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));
    }

    public Optional<ListaEspera> buscarPorToken(String token) throws SQLException {
        return dao.buscarPorToken(token);
    }

    private ListaEspera devolverOferta(Connection conn, Long id, String novoStatus, String resultado)
            throws SQLException {
        dao.travar(conn, id);
        ListaEspera e = dao.buscarPorId(conn, id).orElseThrow(() -> naoEncontrado(id));
        boolean tinhaOferta = ListaEspera.OFERTADA.equals(e.getStatus()) && e.getConsultaOfertadaId() != null;

        if (ListaEspera.AGUARDANDO.equals(novoStatus) && !tinhaOferta) {
            throw new RegraNegocioException("Não há vaga oferecida para este pedido.");
        }
        if (ListaEspera.DESISTIU.equals(novoStatus) && !e.naFila()) {
            throw new RegraNegocioException("O pedido já está " + e.getStatus() + ".");
        }
        dao.atualizarStatus(conn, id, novoStatus, null);

        if (!tinhaOferta) {
            return null;
        }
        dao.registrarResposta(conn, id, e.getConsultaOfertadaId(), resultado);
        consultaDao.travar(conn, e.getConsultaOfertadaId());
        Optional<Consulta> vaga = consultaDao.buscarPorId(conn, e.getConsultaOfertadaId());
        if (vaga.isEmpty() || !vaga.get().getStatus().vagaDevolvida() || vaga.get().jaPassou(Relogio.agora())) {
            return null;
        }
        return realocacaoService.oferecerVaga(conn, vaga.get(), id);
    }

    private Map<Long, Integer> posicoes(Long especialidadeId, Long unidadeId) throws SQLException {
        List<ListaEspera> fila = new ArrayList<>(dao.listarFila(especialidadeId, unidadeId));
        fila.sort(FilaPrioridade.ordem(Relogio.hoje()));
        Map<Long, Integer> posicoes = new HashMap<>();
        for (int i = 0; i < fila.size(); i++) {
            posicoes.put(fila.get(i).getId(), i + 1);
        }
        return posicoes;
    }

    private ListaEspera paraModel(ListaEsperaRequest req, Long ignorarEsperaId) throws SQLException {
        ListaEspera e = new ListaEspera();
        e.setPaciente(pacienteService.obter(req.getPacienteId()));
        e.setEspecialidade(especialidadeService.obter(req.getEspecialidadeId()));
        e.setUnidade(unidadeService.obter(req.getUnidadeId()));
        e.setSolicitante(profissionalService.obter(req.getProfissionalSolicitanteId()));

        if (req.getProfissionalPreferidoId() != null) {
            Profissional preferido = profissionalService.obter(req.getProfissionalPreferidoId());
            if (!preferido.getUnidade().getId().equals(req.getUnidadeId())) {
                throw new RegraNegocioException("O profissional preferido não atende nesta unidade.");
            }
            if (!profissionalDao.atendeEspecialidade(preferido.getId(), req.getEspecialidadeId())) {
                throw new RegraNegocioException("O profissional preferido não atende esta especialidade.");
            }
            e.setProfissionalPreferido(preferido);
        }

        boolean prazoLegal = Boolean.TRUE.equals(req.getPrazoLegal());
        if (prazoLegal && !"P1".equals(req.getClasseRisco())) {
            throw new RegraNegocioException("Só pedidos de classe P1 podem ter prazo legal.");
        }

        Optional<String> existente = dao.statusPedidoExistente(req.getPacienteId(), req.getEspecialidadeId(),
                ignorarEsperaId);
        if (existente.isPresent()) {
            String status = existente.get();
            if (ListaEspera.AGUARDANDO.equals(status) || ListaEspera.OFERTADA.equals(status)) {
                throw new RegraNegocioException("O paciente já está na lista de espera desta especialidade (RN09).");
            }
            throw new RegraNegocioException("O paciente já tem um pedido " + status + " nesta especialidade. "
                    + "Remova o pedido antigo antes de incluir um novo.");
        }

        e.setClasseRisco(req.getClasseRisco());
        e.setJustificativaClinica(req.getJustificativaClinica().trim());
        e.setPrazoLegal(prazoLegal);
        e.setPrioridadeLegal(req.getPrioridadeLegal());
        e.setAceitaMesmoDia(Boolean.TRUE.equals(req.getAceitaMesmoDia()));
        return e;
    }

    private static RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Pedido " + id + " da lista de espera não encontrado.");
    }
}
