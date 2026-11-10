package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UnidadeRequest {

    @NotBlank(message = "Informe o nome da unidade.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o endereço.")
    @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres.")
    private String endereco;

    @NotBlank(message = "Informe o bairro.")
    @Size(max = 60, message = "O bairro deve ter no máximo 60 caracteres.")
    private String bairro;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 60, message = "A cidade deve ter no máximo 60 caracteres.")
    private String cidade;

    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter 10 ou 11 dígitos, só números.")
    private String telefone;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
