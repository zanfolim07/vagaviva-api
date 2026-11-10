package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.dto.ConsultaResponse;
import br.com.fiap.vagaviva.dto.ProfissionalRequest;
import br.com.fiap.vagaviva.dto.ProfissionalResponse;
import br.com.fiap.vagaviva.service.ConsultaService;
import br.com.fiap.vagaviva.service.ProfissionalService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;
    private final ConsultaService consultaService;

    public ProfissionalController(ProfissionalService service, ConsultaService consultaService) {
        this.service = service;
        this.consultaService = consultaService;
    }

    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listar(@RequestParam(required = false) Long unidade,
                                                             @RequestParam(required = false) Long especialidade)
            throws SQLException {
        return ResponseEntity.ok(service.listar(unidade, especialidade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> buscar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> criar(@Valid @RequestBody ProfissionalRequest req) throws SQLException {
        ProfissionalResponse criado = service.criar(req);
        return ResponseEntity.created(Rotas.location("/profissionais", criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody ProfissionalRequest req)
            throws SQLException {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/consultas")
    public ResponseEntity<List<ConsultaResponse>> consultas(@PathVariable Long id,
                                                            @RequestParam(required = false)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data)
            throws SQLException {
        return ResponseEntity.ok(consultaService.listarDoProfissional(id, data));
    }

    @GetMapping("/{id}/horarios-livres")
    public ResponseEntity<List<String>> horariosLivres(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) throws SQLException {
        return ResponseEntity.ok(consultaService.horariosLivres(id, data));
    }
}
