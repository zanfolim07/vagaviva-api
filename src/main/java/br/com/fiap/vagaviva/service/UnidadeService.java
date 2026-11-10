package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.dao.UnidadeDao;
import br.com.fiap.vagaviva.dto.UnidadeRequest;
import br.com.fiap.vagaviva.dto.UnidadeResponse;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.model.Unidade;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class UnidadeService {

    private final UnidadeDao dao;
    private final Conversor conversor;

    public UnidadeService(UnidadeDao dao, Conversor conversor) {
        this.dao = dao;
        this.conversor = conversor;
    }

    public List<UnidadeResponse> listar() throws SQLException {
        return dao.listar().stream().map(conversor::unidade).toList();
    }

    public UnidadeResponse buscar(Long id) throws SQLException {
        return conversor.unidade(obter(id));
    }

    public UnidadeResponse criar(UnidadeRequest req) throws SQLException {
        Unidade u = conversor.copiar(req, Unidade.class);
        Long id = dao.inserir(u);
        return buscar(id);
    }

    public UnidadeResponse atualizar(Long id, UnidadeRequest req) throws SQLException {
        obter(id);
        Unidade u = conversor.copiar(req, Unidade.class);
        u.setId(id);
        dao.atualizar(u);
        return buscar(id);
    }

    public void remover(Long id) throws SQLException {
        if (!dao.remover(id)) {
            throw naoEncontrada(id);
        }
    }

    public Unidade obter(Long id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> naoEncontrada(id));
    }

    private static RecursoNaoEncontradoException naoEncontrada(Long id) {
        return new RecursoNaoEncontradoException("Unidade " + id + " não encontrada.");
    }
}
