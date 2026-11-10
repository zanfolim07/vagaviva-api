package br.com.fiap.vagaviva.service;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class Transacao {

    @FunctionalInterface
    public interface Trabalho<T> {
        T executar(Connection conn) throws SQLException;
    }

    private final DataSource dataSource;

    public Transacao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T executar(Trabalho<T> trabalho) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                T resultado = trabalho.executar(conn);
                conn.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
    }
}
