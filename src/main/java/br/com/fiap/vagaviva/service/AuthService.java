package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.dao.UsuarioDao;
import br.com.fiap.vagaviva.dto.AtivarContaRequest;
import br.com.fiap.vagaviva.dto.ConviteResponse;
import br.com.fiap.vagaviva.dto.LoginRequest;
import br.com.fiap.vagaviva.dto.LoginResponse;
import br.com.fiap.vagaviva.dto.UnidadeResumo;
import br.com.fiap.vagaviva.dto.UsuarioResponse;
import br.com.fiap.vagaviva.exception.AcessoNegadoException;
import br.com.fiap.vagaviva.exception.NaoAutenticadoException;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@Service
public class AuthService {

    private static final String CHAVE_CONVITE = "convite";
    private static final int MAX_FALHAS_LOGIN = 10;
    private static final int MAX_FALHAS_CONVITE = 30;
    private static final String CONVITE_INVALIDO =
            "Código inválido ou expirado. Peça um novo convite ao gestor da unidade.";

    private final UsuarioDao dao;
    private final PasswordEncoder senhas;
    private final TokenService tokenService;
    private final LimiteTentativas limite;
    private final Conversor conversor;
    private final String hashFicticio;

    public AuthService(UsuarioDao dao, PasswordEncoder senhas, TokenService tokenService, LimiteTentativas limite,
                       Conversor conversor) {
        this.dao = dao;
        this.senhas = senhas;
        this.tokenService = tokenService;
        this.limite = limite;
        this.conversor = conversor;
        this.hashFicticio = senhas.encode(UUID.randomUUID().toString());
    }

    public LoginResponse login(LoginRequest req) throws SQLException {
        String email = req.email().trim().toLowerCase();
        String chave = "login:" + email;
        limite.conferir(chave, MAX_FALHAS_LOGIN);

        Usuario u = dao.buscarPorEmail(email).orElse(null);
        boolean temSenha = u != null && u.getSenhaHash() != null;
        boolean senhaCerta = senhas.matches(req.senha(), temSenha ? u.getSenhaHash() : hashFicticio);
        if (!temSenha || !senhaCerta) {
            limite.registrarFalha(chave);
            throw new NaoAutenticadoException("E-mail ou senha inválidos.");
        }
        if (!u.ativo()) {
            throw new AcessoNegadoException("Esta conta está desativada. Fale com o gestor da unidade.");
        }
        limite.limpar(chave);
        u.setUltimoAcesso(Relogio.agora());
        dao.registrarAcesso(u.getId(), u.getUltimoAcesso());

        TokenService.TokenGerado token = tokenService.gerar(u, Boolean.TRUE.equals(req.manterConectado()));
        return new LoginResponse(token.valor(), token.expiraEm(), conversor.usuario(u));
    }

    public ConviteResponse consultarConvite(String codigo) throws SQLException {
        Usuario u = convitePendente(codigo);
        return new ConviteResponse(u.getNome(), u.getEmail(), u.getPerfil(),
                conversor.copiar(u.getUnidade(), UnidadeResumo.class), u.getExpiracaoConvite());
    }

    public UsuarioResponse ativar(AtivarContaRequest req) throws SQLException {
        Usuario u = convitePendente(req.codigoConvite());
        if (req.nome() != null && !req.nome().isBlank()) {
            u.setNome(req.nome().trim());
        }
        u.setSenhaHash(senhas.encode(req.senha()));
        u.setStatus(Usuario.ATIVO);
        u.setCodigoConvite(null);
        u.setExpiracaoConvite(null);
        dao.atualizar(u);
        return conversor.usuario(u);
    }

    private Usuario convitePendente(String codigo) throws SQLException {
        limite.conferir(CHAVE_CONVITE, MAX_FALHAS_CONVITE);
        Usuario u = dao.buscarPorConvite(codigo).orElse(null);
        boolean valido = u != null && Usuario.PENDENTE.equals(u.getStatus())
                && u.getExpiracaoConvite() != null && !u.getExpiracaoConvite().isBefore(Relogio.hoje());
        if (!valido) {
            limite.registrarFalha(CHAVE_CONVITE);
            throw new RecursoNaoEncontradoException(CONVITE_INVALIDO);
        }
        return u;
    }
}
