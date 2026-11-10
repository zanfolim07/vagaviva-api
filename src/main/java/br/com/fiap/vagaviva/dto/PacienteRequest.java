package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class PacienteRequest {

    @NotBlank(message = "Informe o nome do paciente.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o número do Cartão SUS.")
    @Size(min = 15, max = 15, message = "O Cartão SUS tem 15 dígitos.")
    @Pattern(regexp = "\\d+", message = "O Cartão SUS deve ter só números.")
    private String cartaoSus;

    @NotNull(message = "Informe a data de nascimento.")
    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate dataNascimento;

    @NotBlank(message = "Informe o telefone do paciente.")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter 10 ou 11 dígitos, só números.")
    private String telefone;

    @Size(max = 60, message = "O bairro deve ter no máximo 60 caracteres.")
    private String bairro;

    @Pattern(regexp = "WHATSAPP|SMS|LIGACAO", message = "O canal deve ser WHATSAPP, SMS ou LIGACAO.")
    private String canalContato;

    @Pattern(regexp = "\\d{10,11}", message = "O telefone do familiar deve ter 10 ou 11 dígitos, só números.")
    private String telefoneFamiliar;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCartaoSus() {
        return cartaoSus;
    }

    public void setCartaoSus(String cartaoSus) {
        this.cartaoSus = cartaoSus;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCanalContato() {
        return canalContato;
    }

    public void setCanalContato(String canalContato) {
        this.canalContato = canalContato;
    }

    public String getTelefoneFamiliar() {
        return telefoneFamiliar;
    }

    public void setTelefoneFamiliar(String telefoneFamiliar) {
        this.telefoneFamiliar = telefoneFamiliar;
    }
}
