package br.com.fiap.vagaviva.dto;

public class RecusaResponse {

    private ListaEsperaResponse espera;
    private OfertaResponse novaOferta;

    public ListaEsperaResponse getEspera() {
        return espera;
    }

    public void setEspera(ListaEsperaResponse espera) {
        this.espera = espera;
    }

    public OfertaResponse getNovaOferta() {
        return novaOferta;
    }

    public void setNovaOferta(OfertaResponse novaOferta) {
        this.novaOferta = novaOferta;
    }
}
