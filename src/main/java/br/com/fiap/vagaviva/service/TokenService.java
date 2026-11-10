package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.exception.NaoAutenticadoException;
import br.com.fiap.vagaviva.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    public record TokenGerado(String valor, LocalDateTime expiraEm) {
    }

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final int TAMANHO_MINIMO_SEGREDO = 32;
    private static final long HORAS_SESSAO = 8;
    private static final long DIAS_MANTER_CONECTADO = 30;

    private final Algorithm algoritmo;
    private final JWTVerifier verificador;
    private final Map<String, Instant> revogados = new ConcurrentHashMap<>();

    public TokenService(@Value("${jwt.secret:}") String segredo) {
        this.algoritmo = Algorithm.HMAC256(segredoEfetivo(segredo));
        this.verificador = JWT.require(algoritmo).build();
    }

    public TokenGerado gerar(Usuario usuario, boolean manterConectado) {
        LocalDateTime expira = manterConectado
                ? Relogio.agora().plusDays(DIAS_MANTER_CONECTADO)
                : Relogio.agora().plusHours(HORAS_SESSAO);
        String token = JWT.create()
                .withSubject(String.valueOf(usuario.getId()))
                .withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(expira.atZone(ZoneId.of(Relogio.ZONA)).toInstant())
                .sign(algoritmo);
        return new TokenGerado(token, expira);
    }

    public Long validar(String token) {
        DecodedJWT jwt = decodificar(token);
        if (revogados.containsKey(jwt.getId())) {
            throw sessaoExpirada();
        }
        return Long.valueOf(jwt.getSubject());
    }

    public void revogar(String token) {
        DecodedJWT jwt = decodificar(token);
        Instant agora = Instant.now();
        revogados.values().removeIf(expira -> expira.isBefore(agora));
        revogados.put(jwt.getId(), jwt.getExpiresAtAsInstant());
    }

    private DecodedJWT decodificar(String token) {
        try {
            return verificador.verify(token);
        } catch (JWTVerificationException e) {
            throw sessaoExpirada();
        }
    }

    private static NaoAutenticadoException sessaoExpirada() {
        return new NaoAutenticadoException("Sua sessão expirou. Faça login novamente.");
    }

    private static byte[] segredoEfetivo(String segredo) {
        if (segredo == null || segredo.isBlank()) {
            log.warn("JWT_SECRET não definida: usando um segredo temporário. As sessões caem quando a API reiniciar.");
            byte[] aleatorio = new byte[TAMANHO_MINIMO_SEGREDO];
            new SecureRandom().nextBytes(aleatorio);
            return Base64.getEncoder().encode(aleatorio);
        }
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException("JWT_SECRET precisa ter ao menos " + TAMANHO_MINIMO_SEGREDO + " caracteres.");
        }
        return bytes;
    }
}
