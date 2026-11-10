package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ListaEsperaUnidadeRequest(
        @NotNull(message = "Informe o paciente.") @Positive(message = "Informe um paciente válido.")
        Long pacienteId,
        @NotNull(message = "Informe a especialidade.") @Positive(message = "Informe uma especialidade válida.")
        Long especialidadeId,
        @NotNull(message = "Informe o profissional que fez o pedido.")
        @Positive(message = "Informe um profissional válido.") Long profissionalSolicitanteId,
        @Positive(message = "Informe um profissional válido.") Long profissionalPreferidoId,
        @NotNull(message = "Informe a classe de risco.")
        @Pattern(regexp = "P1|P2|P3|P4", message = "A classe de risco deve ser P1, P2, P3 ou P4.")
        String classeRisco,
        @NotBlank(message = "Informe a justificativa clínica.")
        @Size(min = 20, max = 500, message = "A justificativa deve ter entre 20 e 500 caracteres.")
        String justificativaClinica,
        Boolean prazoLegal,
        @Pattern(regexp = "60\\+|80\\+|GESTANTE|PCD",
                message = "A prioridade legal deve ser 60+, 80+, GESTANTE ou PCD.")
        String prioridadeLegal,
        Boolean aceitaMesmoDia) {
}
