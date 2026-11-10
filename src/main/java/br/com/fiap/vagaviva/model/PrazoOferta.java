package br.com.fiap.vagaviva.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class PrazoOferta {

    private static final int MINUTOS_VAGA_PROXIMA = 30;
    private static final int HORAS_VAGA_EM_DIAS = 4;
    private static final int HORAS_MAXIMO = 48;
    private static final int DIAS_ANTES_DA_CONSULTA = 4;
    private static final LocalTime HORA_LIMITE = LocalTime.of(18, 0);

    private PrazoOferta() {
    }

    public static LocalDateTime limite(LocalDateTime agora, LocalDateTime dataConsulta) {
        Duration restante = Duration.between(agora, dataConsulta);
        if (restante.compareTo(Duration.ofHours(24)) < 0) {
            return agora.plusMinutes(Math.min(MINUTOS_VAGA_PROXIMA, restante.toMinutes() / 2));
        }
        LocalDateTime padrao = dataConsulta.toLocalDate().minusDays(DIAS_ANTES_DA_CONSULTA).atTime(HORA_LIMITE);
        if (!padrao.isAfter(agora.plusHours(HORAS_VAGA_EM_DIAS))) {
            return agora.plusHours(HORAS_VAGA_EM_DIAS);
        }
        LocalDateTime maximo = agora.plusHours(HORAS_MAXIMO);
        return padrao.isBefore(maximo) ? padrao : maximo;
    }
}
