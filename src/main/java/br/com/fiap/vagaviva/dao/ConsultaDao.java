package br.com.fiap.vagaviva.dao;

import br.com.fiap.vagaviva.model.Consulta;
import br.com.fiap.vagaviva.model.Especialidade;
import br.com.fiap.vagaviva.model.Profissional;
import br.com.fiap.vagaviva.model.StatusConsulta;
import br.com.fiap.vagaviva.dto.EspecialidadePainelResponse;
import br.com.fiap.vagaviva.dto.SemanaRecuperadasResponse;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ConsultaDao {

    private static final String SELECT =
            "SELECT c.cd_consulta, c.cd_especialidade, e.nm_especialidade, c.dt_agendamento, c.dt_consulta, "
                    + "c.ds_status, c.vl_risco_falta, c.ds_nivel_risco, "
                    + "c.ds_motivo_risco, c.fl_lembrete, c.ds_token_paciente, "
                    + "CASE WHEN (SELECT COUNT(*) FROM tb_consulta hist WHERE hist.cd_paciente = c.cd_paciente "
                    + "AND hist.dt_consulta < c.dt_agendamento AND hist.ds_status IN ('REALIZADA', 'FALTA')) = 0 "
                    + "THEN 1 ELSE 0 END AS primeira_consulta, "
                    + "p.cd_paciente, p.nm_paciente, p.nr_cns, p.dt_nascimento, p.nr_telefone, p.nm_bairro, "
                    + "p.ds_canal_contato, p.nr_telefone_familiar, "
                    + "pr.cd_profissional, pr.nm_profissional, pr.nr_registro, pr.ds_categoria, "
                    + "u.cd_unidade, u.nm_unidade, u.ds_endereco, u.nm_bairro AS u_bairro, u.nm_cidade, "
                    + "u.nr_telefone AS u_telefone "
                    + "FROM tb_consulta c "
                    + "INNER JOIN tb_paciente p ON p.cd_paciente = c.cd_paciente "
                    + "INNER JOIN tb_especialidade e ON e.cd_especialidade = c.cd_especialidade "
                    + "INNER JOIN tb_profissional pr ON pr.cd_profissional = c.cd_profissional "
                    + "INNER JOIN tb_unidade u ON u.cd_unidade = pr.cd_unidade";

    private final DataSource dataSource;

    public ConsultaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Consulta> listar(LocalDate data, Long unidadeId, Long profissionalId, Long pacienteId,
                                 StatusConsulta status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (data != null) {
            sql.append(" AND c.dt_consulta >= ? AND c.dt_consulta < ?");
            parametros.add(data.atStartOfDay());
            parametros.add(data.plusDays(1).atStartOfDay());
        }
        if (unidadeId != null) {
            sql.append(" AND pr.cd_unidade = ?");
            parametros.add(unidadeId);
        }
        if (profissionalId != null) {
            sql.append(" AND c.cd_profissional = ?");
            parametros.add(profissionalId);
        }
        if (pacienteId != null) {
            sql.append(" AND c.cd_paciente = ?");
            parametros.add(pacienteId);
        }
        if (status != null) {
            sql.append(" AND c.ds_status = ?");
            parametros.add(status.name());
        }
        sql.append(" ORDER BY c.dt_consulta");

        List<Consulta> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            JdbcUtil.setParametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(consulta(rs));
                }
            }
        }
        return lista;
    }

    public Optional<Consulta> buscarPorId(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return buscarPorId(conn, id);
        }
    }

    public Optional<Consulta> buscarPorId(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE c.cd_consulta = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(consulta(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Consulta> buscarPorToken(String token) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE c.ds_token_paciente = ?")) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(consulta(rs)) : Optional.empty();
            }
        }
    }

    public boolean travar(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cd_consulta FROM tb_consulta WHERE cd_consulta = ? FOR UPDATE")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static final String HORARIO_PRESO = "(c.ds_status NOT IN ('CANCELADA', 'LIBERADA') OR EXISTS ("
            + "SELECT 1 FROM tb_lista_espera le WHERE le.cd_consulta_ofertada = c.cd_consulta "
            + "AND le.ds_status = 'OFERTADA'))";

    public boolean horarioOcupado(Long profissionalId, LocalDateTime dataConsulta, Long ignorarConsultaId)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_consulta c WHERE c.cd_profissional = ? AND c.dt_consulta = ? "
                + "AND " + HORARIO_PRESO + " AND (? IS NULL OR c.cd_consulta <> ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, profissionalId);
            JdbcUtil.setDataHora(ps, 2, dataConsulta);
            JdbcUtil.setLong(ps, 3, ignorarConsultaId);
            JdbcUtil.setLong(ps, 4, ignorarConsultaId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<LocalTime> horariosOcupados(Long profissionalId, LocalDate data) throws SQLException {
        String sql = "SELECT c.dt_consulta FROM tb_consulta c WHERE c.cd_profissional = ? "
                + "AND c.dt_consulta >= ? AND c.dt_consulta < ? AND " + HORARIO_PRESO;
        List<LocalTime> horarios = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, profissionalId);
            JdbcUtil.setDataHora(ps, 2, data.atStartOfDay());
            JdbcUtil.setDataHora(ps, 3, data.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    horarios.add(rs.getTimestamp("dt_consulta").toLocalDateTime().toLocalTime());
                }
            }
        }
        return horarios;
    }

    public int[] historico(Long pacienteId, LocalDateTime antesDe) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, NVL(SUM(CASE WHEN ds_status = 'FALTA' THEN 1 ELSE 0 END), 0) AS faltas "
                + "FROM tb_consulta WHERE cd_paciente = ? AND ds_status IN ('REALIZADA', 'FALTA') AND dt_consulta < ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pacienteId);
            JdbcUtil.setDataHora(ps, 2, antesDe);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[]{rs.getInt("total"), rs.getInt("faltas")};
            }
        }
    }

    public Long inserir(Consulta c) throws SQLException {
        String sql = "INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, "
                + "dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, "
                + "fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"CD_CONSULTA"})) {
            preencher(ps, c);
            ps.executeUpdate();
            return UnidadeDao.idGerado(ps);
        }
    }

    public boolean atualizar(Consulta c) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return atualizar(conn, c);
        }
    }

    public boolean atualizar(Connection conn, Consulta c) throws SQLException {
        String sql = "UPDATE tb_consulta SET cd_paciente = ?, cd_profissional = ?, cd_especialidade = ?, "
                + "dt_agendamento = ?, dt_consulta = ?, "
                + "ds_status = ?, vl_risco_falta = ?, ds_nivel_risco = ?, ds_motivo_risco = ?, fl_lembrete = ?, "
                + "ds_token_paciente = ? WHERE cd_consulta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            preencher(ps, c);
            ps.setLong(12, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean atualizarStatus(Long id, StatusConsulta status) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return atualizarStatus(conn, id, status);
        }
    }

    public boolean atualizarStatus(Connection conn, Long id, StatusConsulta status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE tb_consulta SET ds_status = ? WHERE cd_consulta = ?")) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean remover(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_consulta WHERE cd_consulta = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int[] contarEncerradas(LocalDate inicio, LocalDate fim, Long unidadeId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, NVL(SUM(CASE WHEN c.ds_status = 'FALTA' THEN 1 ELSE 0 END), 0) AS faltas "
                + "FROM tb_consulta c INNER JOIN tb_profissional pr ON pr.cd_profissional = c.cd_profissional "
                + "WHERE c.ds_status IN ('REALIZADA', 'FALTA') AND c.dt_consulta >= ? AND c.dt_consulta < ? "
                + "AND (? IS NULL OR pr.cd_unidade = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setDataHora(ps, 1, inicio.atStartOfDay());
            JdbcUtil.setDataHora(ps, 2, fim.plusDays(1).atStartOfDay());
            JdbcUtil.setLong(ps, 3, unidadeId);
            JdbcUtil.setLong(ps, 4, unidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[]{rs.getInt("total"), rs.getInt("faltas")};
            }
        }
    }

    public List<EspecialidadePainelResponse> taxaFaltaPorEspecialidade(
            LocalDate inicio, LocalDate fim, Long unidadeId) throws SQLException {
        String sql = "SELECT e.nm_especialidade AS especialidade, COUNT(*) AS consultas, "
                + "SUM(CASE WHEN c.ds_status = 'FALTA' THEN 1 ELSE 0 END) AS faltas "
                + "FROM tb_consulta c INNER JOIN tb_profissional pr ON pr.cd_profissional = c.cd_profissional "
                + "INNER JOIN tb_especialidade e ON e.cd_especialidade = c.cd_especialidade "
                + "WHERE c.ds_status IN ('REALIZADA', 'FALTA') AND c.dt_consulta >= ? AND c.dt_consulta < ? "
                + "AND (? IS NULL OR pr.cd_unidade = ?) GROUP BY e.nm_especialidade ORDER BY e.nm_especialidade";
        List<EspecialidadePainelResponse> resultados = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setDataHora(ps, 1, inicio.atStartOfDay());
            JdbcUtil.setDataHora(ps, 2, fim.plusDays(1).atStartOfDay());
            JdbcUtil.setLong(ps, 3, unidadeId);
            JdbcUtil.setLong(ps, 4, unidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double taxa = rs.getInt("consultas") == 0 ? 0.0
                            : rs.getInt("faltas") / (double) rs.getInt("consultas");
                    resultados.add(new EspecialidadePainelResponse(rs.getString("especialidade"), taxa));
                }
            }
        }
        return resultados;
    }

    public List<SemanaRecuperadasResponse> recuperadasPorSemana(LocalDate inicio, LocalDate fim, Long unidadeId)
            throws SQLException {
        String sql = "SELECT semana, COUNT(*) AS recuperadas FROM ("
                + "SELECT FLOOR((TRUNC(c.dt_agendamento) - TRUNC(?)) / 7) + 1 AS semana "
                + "FROM tb_lista_espera le INNER JOIN tb_consulta c ON c.cd_consulta = le.cd_consulta_ofertada "
                + "WHERE le.ds_status = 'ATENDIDA' AND c.dt_agendamento >= ? AND c.dt_agendamento < ? "
                + "AND (? IS NULL OR le.cd_unidade = ?)) recuperacoes "
                + "GROUP BY semana ORDER BY semana";
        List<SemanaRecuperadasResponse> resultados = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setData(ps, 1, inicio);
            JdbcUtil.setDataHora(ps, 2, inicio.atStartOfDay());
            JdbcUtil.setDataHora(ps, 3, fim.plusDays(1).atStartOfDay());
            JdbcUtil.setLong(ps, 4, unidadeId);
            JdbcUtil.setLong(ps, 5, unidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultados.add(new SemanaRecuperadasResponse(rs.getInt("semana"), rs.getInt("recuperadas")));
                }
            }
        }
        return resultados;
    }

    public int contarVagasDevolvidas(LocalDate inicio, LocalDate fim, Long unidadeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_consulta c "
                + "INNER JOIN tb_profissional pr ON pr.cd_profissional = c.cd_profissional "
                + "WHERE c.ds_status IN ('CANCELADA', 'LIBERADA') AND c.dt_consulta >= ? AND c.dt_consulta < ? "
                + "AND (? IS NULL OR pr.cd_unidade = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setDataHora(ps, 1, inicio.atStartOfDay());
            JdbcUtil.setDataHora(ps, 2, fim.plusDays(1).atStartOfDay());
            JdbcUtil.setLong(ps, 3, unidadeId);
            JdbcUtil.setLong(ps, 4, unidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void preencher(PreparedStatement ps, Consulta c) throws SQLException {
        ps.setLong(1, c.getPaciente().getId());
        ps.setLong(2, c.getProfissional().getId());
        ps.setLong(3, c.getEspecialidade().getId());
        JdbcUtil.setDataHora(ps, 4, c.getDataAgendamento());
        JdbcUtil.setDataHora(ps, 5, c.getDataConsulta());
        ps.setString(6, c.getStatus().name());
        JdbcUtil.setDecimal(ps, 7, c.getRiscoFalta());
        ps.setString(8, c.getNivelRisco());
        ps.setString(9, c.getMotivoRisco());
        ps.setString(10, JdbcUtil.flag(c.isLembrete()));
        ps.setString(11, c.getToken());
    }

    private static Consulta consulta(ResultSet rs) throws SQLException {
        Consulta c = new Consulta();
        c.setId(rs.getLong("cd_consulta"));
        Especialidade especialidade = new Especialidade();
        especialidade.setId(rs.getLong("cd_especialidade"));
        especialidade.setNome(rs.getString("nm_especialidade"));
        c.setEspecialidade(especialidade);
        c.setDataAgendamento(JdbcUtil.dataHora(rs, "dt_agendamento"));
        c.setDataConsulta(JdbcUtil.dataHora(rs, "dt_consulta"));
        c.setStatus(StatusConsulta.valueOf(rs.getString("ds_status")));
        c.setRiscoFalta(JdbcUtil.decimalOuNulo(rs, "vl_risco_falta"));
        c.setNivelRisco(rs.getString("ds_nivel_risco"));
        c.setMotivoRisco(rs.getString("ds_motivo_risco"));
        c.setLembrete(JdbcUtil.flag(rs, "fl_lembrete"));
        c.setToken(rs.getString("ds_token_paciente"));
        c.setPrimeiraConsulta(rs.getInt("primeira_consulta") == 1);
        c.setPaciente(JdbcUtil.paciente(rs));

        Profissional pr = new Profissional();
        pr.setId(rs.getLong("cd_profissional"));
        pr.setNome(rs.getString("nm_profissional"));
        pr.setRegistro(rs.getString("nr_registro"));
        pr.setCategoria(rs.getString("ds_categoria"));
        pr.setUnidade(JdbcUtil.unidade(rs, true));
        c.setProfissional(pr);
        return c;
    }
}
