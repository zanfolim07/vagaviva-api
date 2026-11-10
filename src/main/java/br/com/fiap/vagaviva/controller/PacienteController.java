package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.dto.ConsultaRequest;
import br.com.fiap.vagaviva.dto.ConsultaResponse;
import br.com.fiap.vagaviva.dto.ListaEsperaResponse;
import br.com.fiap.vagaviva.dto.PacienteRequest;
import br.com.fiap.vagaviva.dto.PacienteResponse;
import br.com.fiap.vagaviva.service.ConsultaService;
import br.com.fiap.vagaviva.service.ListaEsperaService;
import br.com.fiap.vagaviva.service.PacienteService;
import jakarta.validation.Valid;
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
import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;
    private final ConsultaService consultaService;
    private final ListaEsperaService listaEsperaService;

    public PacienteController(PacienteService service, ConsultaService consultaService,
                              ListaEsperaService listaEsperaService) {
        this.service = service;
        this.consultaService = consultaService;
        this.listaEsperaService = listaEsperaService;
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar(@RequestParam(required = false) String busca,
                                                         @RequestParam(required = false) String cartaoSus)
            throws SQLException {
        return ResponseEntity.ok(service.listar(busca, cartaoSus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> criar(@Valid @RequestBody PacienteRequest req) throws SQLException {
        PacienteResponse criado = service.criar(req);
        return ResponseEntity.created(Rotas.location("/pacientes", criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest req)
            throws SQLException {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/consultas")
    public ResponseEntity<List<ConsultaResponse>> consultas(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(consultaService.listarDoPaciente(id));
    }

    @PostMapping("/{id}/consultas")
    public ResponseEntity<ConsultaResponse> agendar(@PathVariable Long id, @Valid @RequestBody ConsultaRequest req)
            throws SQLException {
        ConsultaResponse criada = consultaService.agendar(id, req);
        return ResponseEntity.created(Rotas.location("/consultas", criada.getId())).body(criada);
    }

    @GetMapping("/{id}/lista-espera")
    public ResponseEntity<List<ListaEsperaResponse>> listaEspera(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(listaEsperaService.listarDoPaciente(id));
    }
}
