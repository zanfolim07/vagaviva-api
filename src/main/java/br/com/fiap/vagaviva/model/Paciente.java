package br.com.fiap.vagaviva.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class Paciente {

    private Long id;
    private String nome;
    private String cartaoSus;
    private LocalDate dataNascimento;
    private String telefone;
    private String bairro;
    private String canalContato;
    private String telefoneFamiliar;
    private boolean primeiraConsulta;
    private LocalDateTime proximaConsulta;
    private Long proximaConsultaId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isPrimeiraConsulta() {
        return primeiraConsulta;
    }

    public void setPrimeiraConsulta(boolean primeiraConsulta) {
        this.primeiraConsulta = primeiraConsulta;
    }

    public int idadeEm(LocalDate data) {
        return Period.between(dataNascimento, data).getYears();
    }

    public LocalDateTime getProximaConsulta() {
        return proximaConsulta;
    }

    public void setProximaConsulta(LocalDateTime proximaConsulta) {
        this.proximaConsulta = proximaConsulta;
    }

    public Long getProximaConsultaId() {
        return proximaConsultaId;
    }

    public void setProximaConsultaId(Long proximaConsultaId) {
        this.proximaConsultaId = proximaConsultaId;
    }
}
