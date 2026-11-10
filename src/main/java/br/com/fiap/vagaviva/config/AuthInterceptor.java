package br.com.fiap.vagaviva.config;

import br.com.fiap.vagaviva.dao.UsuarioDao;
import br.com.fiap.vagaviva.exception.AcessoNegadoException;
import br.com.fiap.vagaviva.exception.NaoAutenticadoException;
import br.com.fiap.vagaviva.model.Usuario;
import br.com.fiap.vagaviva.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.sql.SQLException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USUARIO = "usuarioAutenticado";
    private static final String PREFIXO = "Bearer ";

    private final TokenService tokenService;
    private final UsuarioDao usuarioDao;

    public AuthInterceptor(TokenService tokenService, UsuarioDao usuarioDao) {
        this.tokenService = tokenService;
        this.usuarioDao = usuarioDao;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws SQLException {
        if (!(handler instanceof HandlerMethod metodo)) {
            return true;
        }
        Usuario usuario = usuarioDao.buscarPorId(tokenService.validar(token(request)))
                .filter(Usuario::ativo)
                .orElseThrow(() -> new NaoAutenticadoException("Sua sessão expirou. Faça login novamente."));
        if (metodo.getBeanType().isAnnotationPresent(SomenteGestao.class) && !usuario.gestao()) {
            throw new AcessoNegadoException("Só o perfil Gestão pode fazer isso.");
        }
        request.setAttribute(USUARIO, usuario);
        return true;
    }

    public static String token(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIXO)) {
            throw new NaoAutenticadoException("Faça login para continuar.");
        }
        return header.substring(PREFIXO.length()).trim();
    }
}
