package br.com.fiap.vagaviva.dto;

import java.util.List;

public class ProfissionalResponse {

    private Long id;
    private String nome;
    private String registro;
    private String categoria;
    private UnidadeResumo unidade;
    private List<EspecialidadeResponse> especialidades;

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

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public UnidadeResumo getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeResumo unidade) {
        this.unidade = unidade;
    }

    public List<EspecialidadeResponse> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<EspecialidadeResponse> especialidades) {
        this.especialidades = especialidades;
    }
}
