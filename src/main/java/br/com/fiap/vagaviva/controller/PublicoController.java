package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.dto.ConsultaPublicaResponse;
import br.com.fiap.vagaviva.dto.FilaPublicaResponse;
import br.com.fiap.vagaviva.dto.MesmoDiaRequest;
import br.com.fiap.vagaviva.dto.PainelResponse;
import br.com.fiap.vagaviva.service.PainelService;
import br.com.fiap.vagaviva.service.PublicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequestMapping("/publico")
public class PublicoController {

    private final PublicoService service;
    private final PainelService painelService;

    public PublicoController(PublicoService service, PainelService painelService) {
        this.service = service;
        this.painelService = painelService;
    }

    @GetMapping("/painel")
    public ResponseEntity<PainelResponse> painel() throws SQLException {
        return ResponseEntity.ok(painelService.gerar(null, null, null));
    }

    @GetMapping("/consultas/{token}")
    public ResponseEntity<ConsultaPublicaResponse> consulta(@PathVariable String token) throws SQLException {
        return ResponseEntity.ok(service.consulta(token));
    }

    @PatchMapping("/consultas/{token}/confirmar")
    public ResponseEntity<ConsultaPublicaResponse> confirmar(@PathVariable String token) throws SQLException {
        return ResponseEntity.ok(service.confirmar(token));
    }

    @PatchMapping("/consultas/{token}/cancelar")
    public ResponseEntity<ConsultaPublicaResponse> cancelar(@PathVariable String token) throws SQLException {
        return ResponseEntity.ok(service.cancelar(token));
    }

    @GetMapping("/fila/{token}")
    public ResponseEntity<FilaPublicaResponse> fila(@PathVariable String token) throws SQLException {
        return ResponseEntity.ok(service.fila(token));
    }

    @PatchMapping("/fila/{token}/aceitar")
    public ResponseEntity<FilaPublicaResponse> aceitar(@PathVariable String token) throws SQLException {
        return ResponseEntity.ok(service.aceitar(token));
    }

    @PatchMapping("/fila/{token}/recusar")
    public ResponseEntity<FilaPublicaResponse> recusar(@PathVariable String token) throws SQLException {
        return ResponseEntity.ok(service.recusar(token));
    }

    @PatchMapping("/fila/{token}/mesmo-dia")
    public ResponseEntity<FilaPublicaResponse> mesmoDia(@PathVariable String token,
                                                        @Valid @RequestBody MesmoDiaRequest req) throws SQLException {
        return ResponseEntity.ok(service.mesmoDia(token, req.getAceita()));
    }

    @PatchMapping("/fila/{token}/sair")
    public ResponseEntity<FilaPublicaResponse> sair(@PathVariable String token) throws SQLException {
        return ResponseEntity.ok(service.sair(token));
    }
}
