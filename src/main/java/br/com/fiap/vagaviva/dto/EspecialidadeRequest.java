package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EspecialidadeRequest {

    @NotBlank(message = "Informe o nome da especialidade.")
    @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres.")
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
