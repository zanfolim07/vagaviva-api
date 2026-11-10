package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.dao.EspecialidadeDao;
import br.com.fiap.vagaviva.dto.EspecialidadeRequest;
import br.com.fiap.vagaviva.dto.EspecialidadeResponse;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.model.Especialidade;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class EspecialidadeService {

    private final EspecialidadeDao dao;
    private final Conversor conversor;

    public EspecialidadeService(EspecialidadeDao dao, Conversor conversor) {
        this.dao = dao;
        this.conversor = conversor;
    }

    public List<EspecialidadeResponse> listar() throws SQLException {
        return dao.listar().stream().map(conversor::especialidade).toList();
    }

    public EspecialidadeResponse buscar(Long id) throws SQLException {
        return conversor.especialidade(obter(id));
    }

    public EspecialidadeResponse criar(EspecialidadeRequest req) throws SQLException {
        Especialidade e = conversor.copiar(req, Especialidade.class);
        return buscar(dao.inserir(e));
    }

    public EspecialidadeResponse atualizar(Long id, EspecialidadeRequest req) throws SQLException {
        obter(id);
        Especialidade e = conversor.copiar(req, Especialidade.class);
        e.setId(id);
        dao.atualizar(e);
        return buscar(id);
    }

    public void remover(Long id) throws SQLException {
        if (!dao.remover(id)) {
            throw naoEncontrada(id);
        }
    }

    public Especialidade obter(Long id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> naoEncontrada(id));
    }

    private static RecursoNaoEncontradoException naoEncontrada(Long id) {
        return new RecursoNaoEncontradoException("Especialidade " + id + " não encontrada.");
    }
}
