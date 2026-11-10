package br.com.fiap.vagaviva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class Relogio {

    public static final String ZONA = "America/Sao_Paulo";
    private static final ZoneId ZONE_ID = ZoneId.of(ZONA);

    private Relogio() {
    }

    public static LocalDateTime agora() {
        return LocalDateTime.now(ZONE_ID).withNano(0);
    }

    public static LocalDate hoje() {
        return LocalDate.now(ZONE_ID);
    }
}
