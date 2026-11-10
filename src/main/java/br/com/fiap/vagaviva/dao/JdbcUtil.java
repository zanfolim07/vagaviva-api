package br.com.fiap.vagaviva.dao;

import br.com.fiap.vagaviva.model.Paciente;
import br.com.fiap.vagaviva.model.Unidade;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

final class JdbcUtil {

    private JdbcUtil() {
    }

    static LocalDate data(ResultSet rs, String coluna) throws SQLException {
        Date d = rs.getDate(coluna);
        return d == null ? null : d.toLocalDate();
    }

    static LocalDateTime dataHora(ResultSet rs, String coluna) throws SQLException {
        Timestamp t = rs.getTimestamp(coluna);
        return t == null ? null : t.toLocalDateTime();
    }

    static Long longOuNulo(ResultSet rs, String coluna) throws SQLException {
        long v = rs.getLong(coluna);
        return rs.wasNull() ? null : v;
    }

    static Integer inteiroOuNulo(ResultSet rs, String coluna) throws SQLException {
        int v = rs.getInt(coluna);
        return rs.wasNull() ? null : v;
    }

    static Double decimalOuNulo(ResultSet rs, String coluna) throws SQLException {
        BigDecimal v = rs.getBigDecimal(coluna);
        return v == null ? null : v.doubleValue();
    }

    static boolean flag(ResultSet rs, String coluna) throws SQLException {
        return "S".equals(rs.getString(coluna));
    }

    static String flag(boolean valor) {
        return valor ? "S" : "N";
    }

    static void setData(PreparedStatement ps, int i, LocalDate valor) throws SQLException {
        if (valor == null) {
            ps.setNull(i, Types.DATE);
        } else {
            ps.setDate(i, Date.valueOf(valor));
        }
    }

    static void setDataHora(PreparedStatement ps, int i, LocalDateTime valor) throws SQLException {
        if (valor == null) {
            ps.setNull(i, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(i, Timestamp.valueOf(valor));
        }
    }

    static void setLong(PreparedStatement ps, int i, Long valor) throws SQLException {
        if (valor == null) {
            ps.setNull(i, Types.NUMERIC);
        } else {
            ps.setLong(i, valor);
        }
    }

    static void setDecimal(PreparedStatement ps, int i, Double valor) throws SQLException {
        if (valor == null) {
            ps.setNull(i, Types.NUMERIC);
        } else {
            ps.setBigDecimal(i, BigDecimal.valueOf(valor));
        }
    }

    static void setInteiro(PreparedStatement ps, int i, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(i, Types.NUMERIC);
        } else {
            ps.setInt(i, valor);
        }
    }

    static void setParametros(PreparedStatement ps, java.util.List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            Object p = parametros.get(i);
            if (p instanceof LocalDate d) {
                setData(ps, i + 1, d);
            } else if (p instanceof LocalDateTime dt) {
                setDataHora(ps, i + 1, dt);
            } else {
                ps.setObject(i + 1, p);
            }
        }
    }

    static Paciente paciente(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId(rs.getLong("cd_paciente"));
        p.setNome(rs.getString("nm_paciente"));
        p.setCartaoSus(rs.getString("nr_cns"));
        p.setDataNascimento(data(rs, "dt_nascimento"));
        p.setTelefone(rs.getString("nr_telefone"));
        p.setBairro(rs.getString("nm_bairro"));
        p.setCanalContato(rs.getString("ds_canal_contato"));
        p.setTelefoneFamiliar(rs.getString("nr_telefone_familiar"));
        return p;
    }

    static Unidade unidade(ResultSet rs, boolean comPrefixo) throws SQLException {
        Unidade u = new Unidade();
        u.setId(rs.getLong("cd_unidade"));
        u.setNome(rs.getString("nm_unidade"));
        u.setEndereco(rs.getString("ds_endereco"));
        u.setBairro(rs.getString(comPrefixo ? "u_bairro" : "nm_bairro"));
        u.setCidade(rs.getString("nm_cidade"));
        u.setTelefone(rs.getString(comPrefixo ? "u_telefone" : "nr_telefone"));
        return u;
    }
}
