package br.com.fiap.vagaviva.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UsuarioResponse {

    private Long id;
    private String nome;
    private String email;
    private String perfil;
    private String status;
    private UnidadeResumo unidade;
    private String codigoConvite;
    private LocalDate expiracaoConvite;
    private LocalDateTime ultimoAcesso;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public UnidadeResumo getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeResumo unidade) {
        this.unidade = unidade;
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
