package br.com.fiap.vagaviva.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

final class Rotas {

    private Rotas() {
    }

    static URI location(String caminho, Object id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(caminho + "/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
