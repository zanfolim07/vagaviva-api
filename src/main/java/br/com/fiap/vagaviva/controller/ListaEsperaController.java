package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.dto.ClasseRiscoResponse;
import br.com.fiap.vagaviva.dto.ConsultaResponse;
import br.com.fiap.vagaviva.dto.ListaEsperaRequest;
import br.com.fiap.vagaviva.dto.ListaEsperaResponse;
import br.com.fiap.vagaviva.dto.RecusaResponse;
import br.com.fiap.vagaviva.service.ListaEsperaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/lista-espera")
public class ListaEsperaController {

    private final ListaEsperaService service;

    public ListaEsperaController(ListaEsperaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ListaEsperaResponse>> listar(@RequestParam(required = false) Long especialidade,
                                                            @RequestParam(required = false) Long unidade,
                                                            @RequestParam(required = false) Long paciente,
                                                            @RequestParam(required = false) String status)
            throws SQLException {
        return ResponseEntity.ok(service.listar(especialidade, unidade, paciente, status));
    }

    @GetMapping("/classes")
    public ResponseEntity<List<ClasseRiscoResponse>> classes() {
        return ResponseEntity.ok(service.classes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListaEsperaResponse> buscar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<ListaEsperaResponse> incluir(@Valid @RequestBody ListaEsperaRequest req) throws SQLException {
        ListaEsperaResponse criado = service.incluir(req);
        return ResponseEntity.created(Rotas.location("/lista-espera", criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListaEsperaResponse> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ListaEsperaRequest req) throws SQLException {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<ConsultaResponse> aceitar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.aceitar(id));
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<RecusaResponse> recusar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.recusar(id));
    }
}
