package br.com.fiap.vagaviva.dto;

import java.time.LocalDate;

public record ConviteResponse(String nome, String email, String perfil, UnidadeResumo unidade,
                              LocalDate expiracaoConvite) {
}
