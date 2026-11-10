package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.dto.EspecialidadeRequest;
import br.com.fiap.vagaviva.dto.EspecialidadeResponse;
import br.com.fiap.vagaviva.service.EspecialidadeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService service;

    public EspecialidadeController(EspecialidadeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<EspecialidadeResponse>> listar() throws SQLException {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadeResponse> buscar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<EspecialidadeResponse> criar(@Valid @RequestBody EspecialidadeRequest req) throws SQLException {
        EspecialidadeResponse criada = service.criar(req);
        return ResponseEntity.created(Rotas.location("/especialidades", criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadeResponse> atualizar(@PathVariable Long id,
                                                           @Valid @RequestBody EspecialidadeRequest req)
            throws SQLException {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
