package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.dto.ListaEsperaResponse;
import br.com.fiap.vagaviva.dto.ListaEsperaUnidadeRequest;
import br.com.fiap.vagaviva.dto.ProfissionalResponse;
import br.com.fiap.vagaviva.dto.UnidadeRequest;
import br.com.fiap.vagaviva.dto.UnidadeResponse;
import br.com.fiap.vagaviva.service.ListaEsperaService;
import br.com.fiap.vagaviva.service.ProfissionalService;
import br.com.fiap.vagaviva.service.UnidadeService;
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
@RequestMapping("/unidades")
public class UnidadeController {

    private final UnidadeService service;
    private final ListaEsperaService listaEsperaService;
    private final ProfissionalService profissionalService;

    public UnidadeController(UnidadeService service, ListaEsperaService listaEsperaService,
                             ProfissionalService profissionalService) {
        this.service = service;
        this.listaEsperaService = listaEsperaService;
        this.profissionalService = profissionalService;
    }

    @GetMapping
    public ResponseEntity<List<UnidadeResponse>> listar() throws SQLException {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadeResponse> buscar(@PathVariable Long id) throws SQLException {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<UnidadeResponse> criar(@Valid @RequestBody UnidadeRequest req) throws SQLException {
        UnidadeResponse criada = service.criar(req);
        return ResponseEntity.created(Rotas.location("/unidades", criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadeResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UnidadeRequest req)
            throws SQLException {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/lista-espera")
    public ResponseEntity<List<ListaEsperaResponse>> listaEspera(@PathVariable Long id,
                                                                 @RequestParam(required = false) Long especialidade)
            throws SQLException {
        return ResponseEntity.ok(listaEsperaService.filaDaUnidade(id, especialidade));
    }

    @PostMapping("/{id}/lista-espera")
    public ResponseEntity<ListaEsperaResponse> incluirNaListaEspera(
            @PathVariable Long id, @Valid @RequestBody ListaEsperaUnidadeRequest req) throws SQLException {
        ListaEsperaResponse criado = listaEsperaService.incluir(id, req);
        return ResponseEntity.created(Rotas.location("/lista-espera", criado.getId())).body(criado);
    }

    @GetMapping("/{id}/profissionais")
    public ResponseEntity<List<ProfissionalResponse>> profissionais(@PathVariable Long id) throws SQLException {
        service.obter(id);
        return ResponseEntity.ok(profissionalService.listar(id, null));
    }
}
