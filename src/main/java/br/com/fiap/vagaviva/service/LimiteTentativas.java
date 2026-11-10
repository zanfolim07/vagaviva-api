package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.exception.MuitasTentativasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LimiteTentativas {

    private static final Duration JANELA = Duration.ofMinutes(15);

    private record Contagem(int falhas, Instant expira) {
    }

    private final Map<String, Contagem> contagens = new ConcurrentHashMap<>();

    public void conferir(String chave, int maximo) {
        Contagem c = contagens.get(chave);
        if (c != null && c.expira().isAfter(Instant.now()) && c.falhas() >= maximo) {
            throw new MuitasTentativasException("Muitas tentativas erradas. Aguarde 15 minutos e tente novamente.");
        }
    }

    public void registrarFalha(String chave) {
        Instant agora = Instant.now();
        contagens.values().removeIf(c -> c.expira().isBefore(agora));
        contagens.merge(chave, new Contagem(1, agora.plus(JANELA)),
                (atual, nova) -> new Contagem(atual.falhas() + 1, atual.expira()));
    }

    public void limpar(String chave) {
        contagens.remove(chave);
    }
}
