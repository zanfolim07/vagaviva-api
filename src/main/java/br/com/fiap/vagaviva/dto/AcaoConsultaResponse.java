package br.com.fiap.vagaviva.dto;

public class AcaoConsultaResponse {

    private ConsultaResponse consulta;
    private OfertaResponse oferta;

    public ConsultaResponse getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaResponse consulta) {
        this.consulta = consulta;
    }

    public OfertaResponse getOferta() {
        return oferta;
    }

    public void setOferta(OfertaResponse oferta) {
        this.oferta = oferta;
    }
}
