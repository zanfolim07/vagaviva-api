package br.com.fiap.vagaviva.dto;

import java.util.List;

public record ExplicacaoRiscoResponse(boolean calculada, List<FatorRiscoResponse> fatores) {
}
