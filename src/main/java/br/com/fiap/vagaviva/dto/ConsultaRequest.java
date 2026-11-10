package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class ConsultaRequest {

    @NotNull(message = "Informe o profissional.")
    @Positive(message = "Informe um profissional válido.")
    private Long profissionalId;

    @Positive(message = "Informe uma especialidade válida.")
    private Long especialidadeId;

    @NotNull(message = "Informe a data e a hora da consulta.")
    @Future(message = "A data da consulta deve ser futura (RN02).")
    private LocalDateTime dataConsulta;

    private Boolean lembrete;

    public Long getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(Long profissionalId) {
        this.profissionalId = profissionalId;
    }

    public Long getEspecialidadeId() {
        return especialidadeId;
    }

    public void setEspecialidadeId(Long especialidadeId) {
        this.especialidadeId = especialidadeId;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }

    public Boolean getLembrete() {
        return lembrete;
    }

    public void setLembrete(Boolean lembrete) {
        this.lembrete = lembrete;
    }
}
