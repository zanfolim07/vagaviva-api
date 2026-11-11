package br.com.fiap.vagaviva.dao;

import br.com.fiap.vagaviva.model.Especialidade;
import br.com.fiap.vagaviva.model.ListaEspera;
import br.com.fiap.vagaviva.model.Profissional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ListaEsperaDao {

    private static final String SELECT =
            "SELECT le.cd_espera, le.dt_entrada, le.ds_classe_risco, le.ds_justificativa_clinica, le.fl_prazo_legal, "
                    + "le.ds_prioridade_legal, le.fl_aceita_mesmo_dia, le.ds_token_paciente, le.nr_ultima_posicao, "
                    + "le.ds_status, le.cd_consulta_ofertada, ov.dt_limite AS ov_limite, "
                    + "p.cd_paciente, p.nm_paciente, p.nr_cns, p.dt_nascimento, p.nr_telefone, p.nm_bairro, "
                    + "p.ds_canal_contato, p.nr_telefone_familiar, "
                    + "e.cd_especialidade, e.nm_especialidade, "
                    + "u.cd_unidade, u.nm_unidade, u.ds_endereco, u.nm_bairro AS u_bairro, u.nm_cidade, "
                    + "u.nr_telefone AS u_telefone, "
                    + "s.cd_profissional AS s_id, s.nm_profissional AS s_nome, s.nr_registro AS s_registro, "
                    + "s.ds_categoria AS s_categoria, "
                    + "pp.cd_profissional AS pp_id, pp.nm_profissional AS pp_nome, pp.nr_registro AS pp_registro, "
                    + "pp.ds_categoria AS pp_categoria, "
                    + "co.dt_consulta AS co_data, "
                    + "cop.cd_profissional AS cop_id, cop.nm_profissional AS cop_nome, cop.nr_registro AS cop_registro, "
                    + "cop.ds_categoria AS cop_categoria "
                    + "FROM tb_lista_espera le "
                    + "INNER JOIN tb_paciente p ON p.cd_paciente = le.cd_paciente "
                    + "INNER JOIN tb_especialidade e ON e.cd_especialidade = le.cd_especialidade "
                    + "INNER JOIN tb_unidade u ON u.cd_unidade = le.cd_unidade "
                    + "INNER JOIN tb_profissional s ON s.cd_profissional = le.cd_profissional_solicitante "
                    + "LEFT JOIN tb_profissional pp ON pp.cd_profissional = le.cd_profissional_pref "
                    + "LEFT JOIN tb_consulta co ON co.cd_consulta = le.cd_consulta_ofertada "
                    + "LEFT JOIN tb_profissional cop ON cop.cd_profissional = co.cd_profissional "
                    + "LEFT JOIN tb_oferta_vaga ov ON ov.cd_espera = le.cd_espera "
                    + "AND ov.cd_consulta = le.cd_consulta_ofertada AND ov.ds_resultado IS NULL";

    private final DataSource dataSource;

    public ListaEsperaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ListaEspera> listar(Long especialidadeId, Long unidadeId, Long pacienteId, String status)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (especialidadeId != null) {
            sql.append(" AND le.cd_especialidade = ?");
            parametros.add(especialidadeId);
        }
        if (unidadeId != null) {
            sql.append(" AND le.cd_unidade = ?");
            parametros.add(unidadeId);
        }
        if (pacienteId != null) {
            sql.append(" AND le.cd_paciente = ?");
            parametros.add(pacienteId);
        }
        if (status != null) {
            sql.append(" AND le.ds_status = ?");
            parametros.add(status);
        }
        sql.append(" ORDER BY le.dt_entrada, le.cd_espera");
        try (Connection conn = dataSource.getConnection()) {
            return consultar(conn, sql.toString(), parametros);
        }
    }

    public List<ListaEspera> listarFila(Long especialidadeId, Long unidadeId) throws SQLException {
        String sql = SELECT + " WHERE le.cd_especialidade = ? AND le.cd_unidade = ? "
                + "AND le.ds_status IN ('AGUARDANDO', 'OFERTADA')";
        List<Object> parametros = new ArrayList<>(List.of(especialidadeId, unidadeId));
        try (Connection conn = dataSource.getConnection()) {
            return consultar(conn, sql, parametros);
        }
    }

    public List<ListaEspera> listarCandidatos(Connection conn, Long consultaId, Long unidadeId, Long profissionalId,
                                              boolean somenteMesmoDia, Long ignorarEsperaId, Long ignorarPacienteId)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT)
                .append(" WHERE le.cd_unidade = ? AND le.ds_status = 'AGUARDANDO'")
                .append(" AND le.cd_especialidade IN (SELECT pe.cd_especialidade FROM tb_profissional_especialidade pe")
                .append(" WHERE pe.cd_profissional = ?)")
                .append(" AND (le.cd_profissional_pref IS NULL OR le.cd_profissional_pref = ?)")
                .append(" AND NOT EXISTS (SELECT 1 FROM tb_oferta_vaga r WHERE r.cd_espera = le.cd_espera")
                .append(" AND r.cd_consulta = ? AND r.ds_resultado IS NOT NULL)");
        List<Object> parametros = new ArrayList<>(List.of(unidadeId, profissionalId, profissionalId, consultaId));
        if (somenteMesmoDia) {
            sql.append(" AND le.fl_aceita_mesmo_dia = 'S'");
        }
        if (ignorarEsperaId != null) {
            sql.append(" AND le.cd_espera <> ?");
            parametros.add(ignorarEsperaId);
        }
        if (ignorarPacienteId != null) {
            sql.append(" AND le.cd_paciente <> ?");
            parametros.add(ignorarPacienteId);
        }
        return consultar(conn, sql.toString(), parametros);
    }

    public Optional<ListaEspera> buscarPorId(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return buscarPorId(conn, id);
        }
    }

    public Optional<ListaEspera> buscarPorId(Connection conn, Long id) throws SQLException {
        List<ListaEspera> lista = consultar(conn, SELECT + " WHERE le.cd_espera = ?", new ArrayList<>(List.of(id)));
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    public Optional<ListaEspera> buscarPorToken(String token) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            List<ListaEspera> lista = consultar(conn, SELECT + " WHERE le.ds_token_paciente = ?",
                    new ArrayList<>(List.of(token)));
            return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
        }
    }

    public boolean travar(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cd_espera FROM tb_lista_espera WHERE cd_espera = ? FOR UPDATE")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Optional<String> statusPedidoExistente(Long pacienteId, Long especialidadeId, Long ignorarEsperaId)
            throws SQLException {
        String sql = "SELECT ds_status FROM tb_lista_espera WHERE cd_paciente = ? AND cd_especialidade = ? "
                + "AND ds_status IN ('AGUARDANDO', 'OFERTADA') "
                + "AND (? IS NULL OR cd_espera <> ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pacienteId);
            ps.setLong(2, especialidadeId);
            JdbcUtil.setLong(ps, 3, ignorarEsperaId);
            JdbcUtil.setLong(ps, 4, ignorarEsperaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        }
    }

    public Long inserir(ListaEspera e) throws SQLException {
        String sql = "INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, "
                + "cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, "
                + "ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, "
                + "nr_ultima_posicao, ds_status) "
                + "VALUES (sq_tb_lista_espera.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"CD_ESPERA"})) {
            ps.setLong(1, e.getPaciente().getId());
            ps.setLong(2, e.getEspecialidade().getId());
            ps.setLong(3, e.getUnidade().getId());
            ps.setLong(4, e.getSolicitante().getId());
            JdbcUtil.setLong(ps, 5, e.getProfissionalPreferido() == null ? null : e.getProfissionalPreferido().getId());
            JdbcUtil.setLong(ps, 6, e.getConsultaOfertadaId());
            JdbcUtil.setData(ps, 7, e.getDataEntrada());
            ps.setString(8, e.getClasseRisco());
            ps.setString(9, e.getJustificativaClinica());
            ps.setString(10, JdbcUtil.flag(e.isPrazoLegal()));
            ps.setString(11, e.getPrioridadeLegal());
            ps.setString(12, JdbcUtil.flag(e.isAceitaMesmoDia()));
            ps.setString(13, e.getToken());
            JdbcUtil.setInteiro(ps, 14, e.getUltimaPosicao());
            ps.setString(15, e.getStatus());
            ps.executeUpdate();
            return UnidadeDao.idGerado(ps);
        }
    }

    public boolean atualizar(ListaEspera e) throws SQLException {
        String sql = "UPDATE tb_lista_espera SET cd_paciente = ?, cd_especialidade = ?, cd_unidade = ?, "
                + "cd_profissional_solicitante = ?, cd_profissional_pref = ?, ds_classe_risco = ?, "
                + "ds_justificativa_clinica = ?, fl_prazo_legal = ?, ds_prioridade_legal = ?, fl_aceita_mesmo_dia = ? "
                + "WHERE cd_espera = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, e.getPaciente().getId());
            ps.setLong(2, e.getEspecialidade().getId());
            ps.setLong(3, e.getUnidade().getId());
            ps.setLong(4, e.getSolicitante().getId());
            JdbcUtil.setLong(ps, 5, e.getProfissionalPreferido() == null ? null : e.getProfissionalPreferido().getId());
            ps.setString(6, e.getClasseRisco());
            ps.setString(7, e.getJustificativaClinica());
            ps.setString(8, JdbcUtil.flag(e.isPrazoLegal()));
            ps.setString(9, e.getPrioridadeLegal());
            ps.setString(10, JdbcUtil.flag(e.isAceitaMesmoDia()));
            ps.setLong(11, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean atualizarStatus(Connection conn, Long id, String status, Long consultaOfertadaId) throws SQLException {
        String sql = "UPDATE tb_lista_espera SET ds_status = ?, cd_consulta_ofertada = ? WHERE cd_espera = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            JdbcUtil.setLong(ps, 2, consultaOfertadaId);
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean ofertar(Connection conn, Long esperaId, Long consultaId, LocalDateTime limite) throws SQLException {
        String sql = "UPDATE tb_lista_espera SET ds_status = 'OFERTADA', cd_consulta_ofertada = ? "
                + "WHERE cd_espera = ? AND ds_status = 'AGUARDANDO'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, consultaId);
            ps.setLong(2, esperaId);
            if (ps.executeUpdate() == 0) {
                return false;
            }
        }
        String registro = "MERGE INTO tb_oferta_vaga o USING (SELECT ? AS esp, ? AS con FROM dual) d "
                + "ON (o.cd_espera = d.esp AND o.cd_consulta = d.con) "
                + "WHEN MATCHED THEN UPDATE SET o.dt_limite = ?, o.ds_resultado = NULL "
                + "WHEN NOT MATCHED THEN INSERT (cd_espera, cd_consulta, dt_limite) VALUES (d.esp, d.con, ?)";
        try (PreparedStatement ps = conn.prepareStatement(registro)) {
            ps.setLong(1, esperaId);
            ps.setLong(2, consultaId);
            JdbcUtil.setDataHora(ps, 3, limite);
            JdbcUtil.setDataHora(ps, 4, limite);
            ps.executeUpdate();
        }
        return true;
    }

    public void registrarResposta(Connection conn, Long esperaId, Long consultaId, String resultado) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tb_oferta_vaga SET ds_resultado = ? WHERE cd_espera = ? AND cd_consulta = ?")) {
            ps.setString(1, resultado);
            ps.setLong(2, esperaId);
            ps.setLong(3, consultaId);
            ps.executeUpdate();
        }
    }

    public void limparOfertas(Connection conn, Long consultaId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_oferta_vaga WHERE cd_consulta = ?")) {
            ps.setLong(1, consultaId);
            ps.executeUpdate();
        }
    }

    public List<Long> listarOfertasVencidas(LocalDateTime agora) throws SQLException {
        String sql = "SELECT le.cd_espera FROM tb_lista_espera le "
                + "INNER JOIN tb_oferta_vaga ov ON ov.cd_espera = le.cd_espera AND ov.cd_consulta = le.cd_consulta_ofertada "
                + "WHERE le.ds_status = 'OFERTADA' AND ov.ds_resultado IS NULL AND ov.dt_limite <= ?";
        List<Long> ids = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setDataHora(ps, 1, agora);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
            }
        }
        return ids;
    }

    public boolean atualizarStatus(Long id, String status, Long consultaOfertadaId) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return atualizarStatus(conn, id, status, consultaOfertadaId);
        }
    }

    public boolean atualizarMesmoDia(Long id, boolean aceita) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE tb_lista_espera SET fl_aceita_mesmo_dia = ? WHERE cd_espera = ?")) {
            ps.setString(1, JdbcUtil.flag(aceita));
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public void atualizarUltimaPosicao(Long id, Integer posicao) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE tb_lista_espera SET nr_ultima_posicao = ? WHERE cd_espera = ?")) {
            JdbcUtil.setInteiro(ps, 1, posicao);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public Integer buscarPosicaoHistorica(Long id, LocalDate dataReferencia) throws SQLException {
        String sql = "SELECT nr_posicao FROM tb_historico_posicao_fila "
                + "WHERE cd_espera = ? AND dt_referencia <= ? "
                + "ORDER BY dt_referencia DESC FETCH FIRST 1 ROW ONLY";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            JdbcUtil.setData(ps, 2, dataReferencia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public void registrarPosicaoDiaria(Long id, LocalDate dataReferencia, Integer posicao) throws SQLException {
        String sql = "MERGE INTO tb_historico_posicao_fila h "
                + "USING (SELECT ? AS cd_espera, ? AS dt_referencia, ? AS nr_posicao FROM dual) d "
                + "ON (h.cd_espera = d.cd_espera AND h.dt_referencia = d.dt_referencia) "
                + "WHEN NOT MATCHED THEN INSERT (cd_espera, dt_referencia, nr_posicao) "
                + "VALUES (d.cd_espera, d.dt_referencia, d.nr_posicao)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            JdbcUtil.setData(ps, 2, dataReferencia);
            ps.setInt(3, posicao);
            ps.executeUpdate();
        }
    }

    public boolean remover(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_lista_espera WHERE cd_espera = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int contarVagasRecuperadas(LocalDate inicio, LocalDate fim, Long unidadeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_lista_espera le "
                + "INNER JOIN tb_consulta c ON c.cd_consulta = le.cd_consulta_ofertada "
                + "WHERE le.ds_status = 'ATENDIDA' AND c.dt_agendamento >= ? AND c.dt_agendamento < ? "
                + "AND (? IS NULL OR le.cd_unidade = ?)";
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

    private List<ListaEspera> consultar(Connection conn, String sql, List<Object> parametros) throws SQLException {
        List<ListaEspera> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setParametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(listaEspera(rs));
                }
            }
        }
        return lista;
    }

    private static ListaEspera listaEspera(ResultSet rs) throws SQLException {
        ListaEspera le = new ListaEspera();
        le.setId(rs.getLong("cd_espera"));
        le.setDataEntrada(JdbcUtil.data(rs, "dt_entrada"));
        le.setClasseRisco(rs.getString("ds_classe_risco"));
        le.setJustificativaClinica(rs.getString("ds_justificativa_clinica"));
        le.setPrazoLegal(JdbcUtil.flag(rs, "fl_prazo_legal"));
        le.setPrioridadeLegal(rs.getString("ds_prioridade_legal"));
        le.setAceitaMesmoDia(JdbcUtil.flag(rs, "fl_aceita_mesmo_dia"));
        le.setToken(rs.getString("ds_token_paciente"));
        le.setUltimaPosicao(JdbcUtil.inteiroOuNulo(rs, "nr_ultima_posicao"));
        le.setStatus(rs.getString("ds_status"));
        le.setConsultaOfertadaId(JdbcUtil.longOuNulo(rs, "cd_consulta_ofertada"));
        le.setDataConsultaOfertada(JdbcUtil.dataHora(rs, "co_data"));
        le.setLimiteOferta(JdbcUtil.dataHora(rs, "ov_limite"));
        le.setPaciente(JdbcUtil.paciente(rs));

        Especialidade e = new Especialidade();
        e.setId(rs.getLong("cd_especialidade"));
        e.setNome(rs.getString("nm_especialidade"));
        le.setEspecialidade(e);

        le.setUnidade(JdbcUtil.unidade(rs, true));
        le.setSolicitante(profissional(rs, "s"));
        le.setProfissionalPreferido(profissional(rs, "pp"));
        le.setProfissionalConsultaOfertada(profissional(rs, "cop"));
        return le;
    }

    private static Profissional profissional(ResultSet rs, String prefixo) throws SQLException {
        Long id = JdbcUtil.longOuNulo(rs, prefixo + "_id");
        if (id == null) {
            return null;
        }
        Profissional p = new Profissional();
        p.setId(id);
        p.setNome(rs.getString(prefixo + "_nome"));
        p.setRegistro(rs.getString(prefixo + "_registro"));
        p.setCategoria(rs.getString(prefixo + "_categoria"));
        return p;
    }
}
