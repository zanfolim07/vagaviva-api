package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.config.AuthInterceptor;
import br.com.fiap.vagaviva.dto.AtivarContaRequest;
import br.com.fiap.vagaviva.dto.ConviteResponse;
import br.com.fiap.vagaviva.dto.LoginRequest;
import br.com.fiap.vagaviva.dto.LoginResponse;
import br.com.fiap.vagaviva.dto.UsuarioResponse;
import br.com.fiap.vagaviva.model.Usuario;
import br.com.fiap.vagaviva.service.AuthService;
import br.com.fiap.vagaviva.service.Conversor;
import br.com.fiap.vagaviva.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final TokenService tokenService;
    private final Conversor conversor;

    public AuthController(AuthService service, TokenService tokenService, Conversor conversor) {
        this.service = service;
        this.tokenService = tokenService;
        this.conversor = conversor;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) throws SQLException {
        return ResponseEntity.ok(service.login(req));
    }

    @GetMapping("/convite/{codigo}")
    public ResponseEntity<ConviteResponse> convite(@PathVariable String codigo) throws SQLException {
        return ResponseEntity.ok(service.consultarConvite(codigo));
    }

    @PostMapping("/ativar")
    public ResponseEntity<UsuarioResponse> ativar(@Valid @RequestBody AtivarContaRequest req) throws SQLException {
        return ResponseEntity.ok(service.ativar(req));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@RequestAttribute(AuthInterceptor.USUARIO) Usuario usuario) {
        return ResponseEntity.ok(conversor.usuario(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        tokenService.revogar(AuthInterceptor.token(request));
        return ResponseEntity.noContent().build();
    }
}
