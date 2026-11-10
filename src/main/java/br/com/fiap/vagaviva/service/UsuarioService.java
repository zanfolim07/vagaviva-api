package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.dao.UsuarioDao;
import br.com.fiap.vagaviva.dto.UsuarioRequest;
import br.com.fiap.vagaviva.dto.UsuarioResponse;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.exception.RegraNegocioException;
import br.com.fiap.vagaviva.model.Usuario;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;

@Service
public class UsuarioService {

    private static final int DIAS_CONVITE = 7;
    private static final int TENTATIVAS_CODIGO = 20;

    private final SecureRandom aleatorio = new SecureRandom();
    private final UsuarioDao dao;
    private final UnidadeService unidadeService;
    private final Conversor conversor;

    public UsuarioService(UsuarioDao dao, UnidadeService unidadeService, Conversor conversor) {
        this.dao = dao;
        this.unidadeService = unidadeService;
        this.conversor = conversor;
    }

    public List<UsuarioResponse> listar(Long unidadeId) throws SQLException {
        return dao.listar(unidadeId).stream().map(conversor::usuario).toList();
    }

    public UsuarioResponse buscar(Long id) throws SQLException {
        return conversor.usuario(obter(id));
    }

    public UsuarioResponse criar(UsuarioRequest req) throws SQLException {
        Usuario u = new Usuario();
        preencher(u, req);
        u.setStatus(Usuario.PENDENTE);
        gerarConvite(u);
        return buscar(dao.inserir(u));
    }

    public UsuarioResponse atualizar(Long id, UsuarioRequest req, Usuario atual) throws SQLException {
        Usuario u = obter(id);
        if (u.getId().equals(atual.getId()) && !u.getPerfil().equals(req.perfil())) {
            throw new RegraNegocioException("Você não pode mudar o seu próprio perfil.");
        }
        preencher(u, req);
        dao.atualizar(u);
        return buscar(id);
    }

    public void remover(Long id, Usuario atual) throws SQLException {
        conferirOutraPessoa(id, atual, "remover");
        if (!dao.remover(id)) {
            throw naoEncontrado(id);
        }
    }

    public UsuarioResponse reenviarConvite(Long id) throws SQLException {
        Usuario u = obter(id);
        if (!Usuario.PENDENTE.equals(u.getStatus())) {
            throw new RegraNegocioException("Só contas com convite pendente podem receber um novo convite.");
        }
        gerarConvite(u);
        dao.atualizar(u);
        return buscar(id);
    }

    public UsuarioResponse desativar(Long id, Usuario atual) throws SQLException {
        conferirOutraPessoa(id, atual, "desativar");
        Usuario u = obter(id);
        if (Usuario.DESATIVADO.equals(u.getStatus())) {
            throw new RegraNegocioException("A conta já está desativada.");
        }
        u.setStatus(Usuario.DESATIVADO);
        u.setCodigoConvite(null);
        u.setExpiracaoConvite(null);
        dao.atualizar(u);
        return buscar(id);
    }

    public UsuarioResponse reativar(Long id) throws SQLException {
        Usuario u = obter(id);
        if (!Usuario.DESATIVADO.equals(u.getStatus())) {
            throw new RegraNegocioException("A conta não está desativada.");
        }
        if (u.getSenhaHash() != null) {
            u.setStatus(Usuario.ATIVO);
        } else {
            u.setStatus(Usuario.PENDENTE);
            gerarConvite(u);
        }
        dao.atualizar(u);
        return buscar(id);
    }

    public Usuario obter(Long id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));
    }

    private void preencher(Usuario u, UsuarioRequest req) throws SQLException {
        u.setNome(req.nome().trim());
        u.setEmail(req.email().trim().toLowerCase());
        u.setPerfil(req.perfil());
        u.setUnidade(unidadeService.obter(req.unidadeId()));
    }

    private void gerarConvite(Usuario u) throws SQLException {
        u.setCodigoConvite(novoCodigo());
        u.setExpiracaoConvite(Relogio.hoje().plusDays(DIAS_CONVITE));
    }

    private String novoCodigo() throws SQLException {
        for (int i = 0; i < TENTATIVAS_CODIGO; i++) {
            String codigo = String.format("VV-%04d", aleatorio.nextInt(10_000));
            if (dao.buscarPorConvite(codigo).isEmpty()) {
                return codigo;
            }
        }
        throw new RegraNegocioException("Não foi possível gerar o código de convite. Tente novamente.");
    }

    private static void conferirOutraPessoa(Long id, Usuario atual, String acao) {
        if (id.equals(atual.getId())) {
            throw new RegraNegocioException("Você não pode " + acao + " a sua própria conta.");
        }
    }

    private static RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Usuário " + id + " não encontrado.");
    }
}
