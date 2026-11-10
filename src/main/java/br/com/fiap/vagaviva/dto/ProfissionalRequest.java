package br.com.fiap.vagaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ProfissionalRequest {

    @NotNull(message = "Informe a unidade.")
    @Positive(message = "Informe uma unidade válida.")
    private Long unidadeId;

    @NotBlank(message = "Informe o nome do profissional.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o registro no conselho (CRM ou COREN).")
    @Size(max = 20, message = "O registro deve ter no máximo 20 caracteres.")
    private String registro;

    @NotBlank(message = "Informe a categoria.")
    @Pattern(regexp = "MEDICO|ENFERMEIRO", message = "A categoria deve ser MEDICO ou ENFERMEIRO.")
    private String categoria;

    @NotEmpty(message = "Informe ao menos uma especialidade.")
    private List<@NotNull @Positive Long> especialidadeIds;

    public Long getUnidadeId() {
        return unidadeId;
    }

    public void setUnidadeId(Long unidadeId) {
        this.unidadeId = unidadeId;
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

    public List<Long> getEspecialidadeIds() {
        return especialidadeIds;
    }

    public void setEspecialidadeIds(List<Long> especialidadeIds) {
        this.especialidadeIds = especialidadeIds;
    }
}
