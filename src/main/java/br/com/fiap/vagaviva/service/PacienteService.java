package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.dao.PacienteDao;
import br.com.fiap.vagaviva.dto.PacienteRequest;
import br.com.fiap.vagaviva.dto.PacienteResponse;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.model.Paciente;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class PacienteService {

    private final PacienteDao dao;
    private final Conversor conversor;

    public PacienteService(PacienteDao dao, Conversor conversor) {
        this.dao = dao;
        this.conversor = conversor;
    }

    public List<PacienteResponse> listar(String busca, String cartaoSus) throws SQLException {
        return dao.listar(busca, cartaoSus).stream().map(conversor::paciente).toList();
    }

    public PacienteResponse buscar(Long id) throws SQLException {
        return conversor.paciente(obter(id));
    }

    public PacienteResponse criar(PacienteRequest req) throws SQLException {
        return buscar(dao.inserir(paraModel(req)));
    }

    public PacienteResponse atualizar(Long id, PacienteRequest req) throws SQLException {
        obter(id);
        Paciente p = paraModel(req);
        p.setId(id);
        dao.atualizar(p);
        return buscar(id);
    }

    public void remover(Long id) throws SQLException {
        if (!dao.remover(id)) {
            throw naoEncontrado(id);
        }
    }

    public Paciente obter(Long id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));
    }

    private Paciente paraModel(PacienteRequest req) {
        Paciente p = conversor.copiar(req, Paciente.class);
        p.setNome(req.getNome().trim());
        if (p.getCanalContato() == null) {
            p.setCanalContato("WHATSAPP");
        }
        if (p.getBairro() != null && p.getBairro().isBlank()) {
            p.setBairro(null);
        }
        return p;
    }

    private static RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Paciente " + id + " não encontrado.");
    }
}
