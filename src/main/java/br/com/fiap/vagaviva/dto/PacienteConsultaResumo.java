package br.com.fiap.vagaviva.dto;

public class PacienteConsultaResumo {

    private Long id;
    private String nome;
    private String canalContato;
    private boolean primeiraConsulta;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCanalContato() { return canalContato; }
    public void setCanalContato(String canalContato) { this.canalContato = canalContato; }
    public boolean isPrimeiraConsulta() { return primeiraConsulta; }
    public void setPrimeiraConsulta(boolean primeiraConsulta) { this.primeiraConsulta = primeiraConsulta; }
}
