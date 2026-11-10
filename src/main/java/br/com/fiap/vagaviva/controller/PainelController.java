package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.dto.PainelResponse;
import br.com.fiap.vagaviva.service.PainelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.SQLException;
import java.time.LocalDate;

@RestController
@RequestMapping("/painel")
public class PainelController {

    private final PainelService service;

    public PainelController(PainelService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PainelResponse> painel(
            @RequestParam(required = false) Long unidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim)
            throws SQLException {
        return ResponseEntity.ok(service.gerar(unidade, inicio, fim));
    }
}
