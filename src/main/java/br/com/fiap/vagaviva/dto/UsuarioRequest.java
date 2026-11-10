package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.") String nome,
        @NotBlank(message = "Informe o e-mail.") @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres.") String email,
        @NotBlank(message = "Informe o perfil.")
        @Pattern(regexp = "RECEPCAO|GESTAO", message = "O perfil deve ser RECEPCAO ou GESTAO.") String perfil,
        @NotNull(message = "Informe a unidade.") @Positive(message = "Informe uma unidade válida.") Long unidadeId) {
}
