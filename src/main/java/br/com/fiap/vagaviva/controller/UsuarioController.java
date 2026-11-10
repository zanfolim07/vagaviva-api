package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.config.AuthInterceptor;
import br.com.fiap.vagaviva.config.SomenteGestao;
import br.com.fiap.vagaviva.dto.UsuarioRequest;
import br.com.fiap.vagaviva.dto.UsuarioResponse;
import br.com.fiap.vagaviva.model.Usuario;
import br.com.fiap.vagaviva.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@SomenteGestao
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(@RequestParam(required = false) Long unidade)
            throws SQLException {
        return ResponseEntity.ok(service.listar(unidade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest req) throws SQLException {
        UsuarioResponse criado = service.criar(req);
        return ResponseEntity.created(Rotas.location("/usuarios", criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req,
                                                     @RequestAttribute(AuthInterceptor.USUARIO) Usuario atual)
            throws SQLException {
        return ResponseEntity.ok(service.atualizar(id, req, atual));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id,
                                        @RequestAttribute(AuthInterceptor.USUARIO) Usuario atual) throws SQLException {
        service.remover(id, atual);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/convite")
    public ResponseEntity<UsuarioResponse> convite(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.reenviarConvite(id));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<UsuarioResponse> desativar(@PathVariable Long id,
                                                     @RequestAttribute(AuthInterceptor.USUARIO) Usuario atual)
            throws SQLException {
        return ResponseEntity.ok(service.desativar(id, atual));
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<UsuarioResponse> reativar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.reativar(id));
    }
}
