package br.com.fiap.vagaviva.controller;

import br.com.fiap.vagaviva.Relogio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class StatusController {

    @GetMapping("/")
    public Map<String, Object> status() {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("api", "Vaga Viva");
        resposta.put("status", "ok");
        resposta.put("horario", Relogio.agora());
        return resposta;
    }
}
