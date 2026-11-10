package br.com.fiap.vagaviva.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Usuario {

    public static final String GESTAO = "GESTAO";
    public static final String PENDENTE = "PENDENTE";
    public static final String ATIVO = "ATIVO";
    public static final String DESATIVADO = "DESATIVADO";

    private Long id;
    private Unidade unidade;
    private String nome;
    private String email;
    private String senhaHash;
    private String perfil;
    private String status;
    private String codigoConvite;
    private LocalDate expiracaoConvite;
    private LocalDateTime ultimoAcesso;

    public boolean ativo() {
        return ATIVO.equals(status);
    }

    public boolean gestao() {
        return GESTAO.equals(perfil);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCodigoConvite() {
        return codigoConvite;
    }

    public void setCodigoConvite(String codigoConvite) {
        this.codigoConvite = codigoConvite;
    }

    public LocalDate getExpiracaoConvite() {
        return expiracaoConvite;
    }

    public void setExpiracaoConvite(LocalDate expiracaoConvite) {
        this.expiracaoConvite = expiracaoConvite;
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }
}
