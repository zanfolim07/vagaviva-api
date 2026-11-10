package br.com.fiap.vagaviva.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
public class IaClient {

    private static final Logger log = LoggerFactory.getLogger(IaClient.class);
    private static final Set<String> NIVEIS = Set.of("BAIXO", "MEDIO", "ALTO");
    private static final Duration AQUECIMENTO = Duration.ofSeconds(90);

    private final String url;
    private final Duration timeout;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public IaClient(@Value("${ia.url:}") String url,
                    @Value("${ia.timeout-ms:2000}") long timeoutMs,
                    ObjectMapper objectMapper) {
        this.url = url == null ? "" : url.trim().replaceAll("/+$", "");
        this.timeout = Duration.ofMillis(timeoutMs);
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${ia.aquecimento-ms:600000}")
    public void aquecer() {
        if (url.isEmpty()) {
            return;
        }
        httpClient.sendAsync(HttpRequest.newBuilder(URI.create(url + "/")).timeout(AQUECIMENTO).GET().build(),
                HttpResponse.BodyHandlers.discarding());
    }

    public Optional<IaResponse> preverFalta(IaRequest entrada) {
        if (url.isEmpty()) {
            return Optional.empty();
        }
        try {
            HttpRequest requisicao = HttpRequest.newBuilder(URI.create(url + "/prever-falta"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(entrada)))
                    .build();
            HttpResponse<String> resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() != 200) {
                log.warn("IA respondeu HTTP {}. Consulta será salva com risco NAO_CALCULADO.", resposta.statusCode());
                return Optional.empty();
            }
            IaResponse ia = objectMapper.readValue(resposta.body(), IaResponse.class);
            boolean valida = ia.getProbabilidadeFalta() != null
                    && ia.getProbabilidadeFalta() >= 0 && ia.getProbabilidadeFalta() <= 1
                    && ia.getNivel() != null && NIVEIS.contains(ia.getNivel().toUpperCase());
            if (!valida) {
                log.warn("Resposta da IA fora do formato esperado. Consulta será salva com risco NAO_CALCULADO.");
                return Optional.empty();
            }
            ia.setNivel(ia.getNivel().toUpperCase());
            return Optional.of(ia);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            log.warn("IA indisponível ({}). Consulta será salva com risco NAO_CALCULADO.", e.getClass().getSimpleName());
            return Optional.empty();
        }
    }
}
