package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.dao.ProfissionalDao;
import br.com.fiap.vagaviva.dto.ProfissionalRequest;
import br.com.fiap.vagaviva.dto.ProfissionalResponse;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.model.Especialidade;
import br.com.fiap.vagaviva.model.Profissional;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ProfissionalService {

    private final ProfissionalDao dao;
    private final UnidadeService unidadeService;
    private final EspecialidadeService especialidadeService;
    private final Transacao transacao;
    private final Conversor conversor;

    public ProfissionalService(ProfissionalDao dao, UnidadeService unidadeService,
                               EspecialidadeService especialidadeService, Transacao transacao, Conversor conversor) {
        this.dao = dao;
        this.unidadeService = unidadeService;
        this.especialidadeService = especialidadeService;
        this.transacao = transacao;
        this.conversor = conversor;
    }

    public List<ProfissionalResponse> listar(Long unidadeId, Long especialidadeId) throws SQLException {
        return dao.listar(unidadeId, especialidadeId).stream().map(conversor::profissional).toList();
    }

    public ProfissionalResponse buscar(Long id) throws SQLException {
        return conversor.profissional(obter(id));
    }

    public ProfissionalResponse criar(ProfissionalRequest req) throws SQLException {
        Profissional p = paraModel(req);
        Long id = transacao.executar(conn -> dao.inserir(conn, p));
        return buscar(id);
    }

    public ProfissionalResponse atualizar(Long id, ProfissionalRequest req) throws SQLException {
        obter(id);
        Profissional p = paraModel(req);
        p.setId(id);
        transacao.executar(conn -> dao.atualizar(conn, p));
        return buscar(id);
    }

    public void remover(Long id) throws SQLException {
        boolean removido = transacao.executar(conn -> dao.remover(conn, id));
        if (!removido) {
            throw naoEncontrado(id);
        }
    }

    public Profissional obter(Long id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));
    }

    private Profissional paraModel(ProfissionalRequest req) throws SQLException {
        Profissional p = new Profissional();
        p.setNome(req.getNome().trim());
        p.setRegistro(req.getRegistro().trim());
        p.setCategoria(req.getCategoria());
        p.setUnidade(unidadeService.obter(req.getUnidadeId()));
        List<Especialidade> especialidades = new ArrayList<>();
        for (Long especialidadeId : new LinkedHashSet<>(req.getEspecialidadeIds())) {
            especialidades.add(especialidadeService.obter(especialidadeId));
        }
        p.setEspecialidades(especialidades);
        return p;
    }

    private static RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Profissional " + id + " não encontrado.");
    }
}
