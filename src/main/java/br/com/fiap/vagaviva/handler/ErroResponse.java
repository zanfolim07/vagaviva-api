package br.com.fiap.vagaviva.handler;

import java.time.LocalDateTime;
import java.util.List;

public class ErroResponse {

    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private LocalDateTime timestamp;
    private List<CampoErro> campos;

    public ErroResponse(int status, String erro, String mensagem, String caminho, List<CampoErro> campos) {
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.timestamp = br.com.fiap.vagaviva.Relogio.agora();
        this.campos = campos == null ? List.of() : campos;
    }

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<CampoErro> getCampos() {
        return campos;
    }

    public static class CampoErro {

        private final String campo;
        private final String mensagem;

        public CampoErro(String campo, String mensagem) {
            this.campo = campo;
            this.mensagem = mensagem;
        }

        public String getCampo() {
            return campo;
        }

        public String getMensagem() {
            return mensagem;
        }
    }
}
