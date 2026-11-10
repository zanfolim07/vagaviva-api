package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtivarContaRequest(
        @NotBlank(message = "Informe o código de convite.")
        @Pattern(regexp = "VV-\\d{4}", message = "O código tem o formato VV-0000.") String codigoConvite,
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.") String nome,
        @NotBlank(message = "Crie uma senha.")
        @Pattern(regexp = "(?=.*\\d)(?=.*[A-Z]).{8,72}",
                message = "A senha precisa de 8 a 72 caracteres, com ao menos um número e uma letra maiúscula.") String senha) {
}
