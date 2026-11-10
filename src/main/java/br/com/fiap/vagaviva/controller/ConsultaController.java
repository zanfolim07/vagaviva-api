package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.dto.AcaoConsultaResponse;
import br.com.fiap.vagaviva.dto.ConsultaRequest;
import br.com.fiap.vagaviva.dto.ConsultaResponse;
import br.com.fiap.vagaviva.dto.ExplicacaoRiscoResponse;
import br.com.fiap.vagaviva.dto.PresencaRequest;
import br.com.fiap.vagaviva.model.StatusConsulta;
import br.com.fiap.vagaviva.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Long unidade,
            @RequestParam(required = false) Long profissional,
            @RequestParam(required = false) StatusConsulta status) throws SQLException {
        return ResponseEntity.ok(service.listar(data, unidade, profissional, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> buscar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/{id}/risco/explicacao")
    public ResponseEntity<ExplicacaoRiscoResponse> explicarRisco(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.explicarRisco(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaResponse> remarcar(@PathVariable Long id, @Valid @RequestBody ConsultaRequest req)
            throws SQLException {
        return ResponseEntity.ok(service.remarcar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ConsultaResponse> confirmar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.confirmar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AcaoConsultaResponse> cancelar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @PatchMapping("/{id}/liberar")
    public ResponseEntity<AcaoConsultaResponse> liberar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.liberar(id));
    }

    @PatchMapping("/{id}/presenca")
    public ResponseEntity<ConsultaResponse> presenca(@PathVariable Long id, @Valid @RequestBody PresencaRequest req)
            throws SQLException {
        return ResponseEntity.ok(service.registrarPresenca(id, req.getCompareceu()));
    }
}
