package br.com.fiap.vagaviva.dao;

import br.com.fiap.vagaviva.model.Paciente;
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
public class PacienteDao {

    private static final String FUTURAS = "FROM tb_consulta c WHERE c.cd_paciente = p.cd_paciente "
            + "AND c.ds_status IN ('AGENDADA', 'CONFIRMADA') "
            + "AND c.dt_consulta >= CAST(SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo' AS DATE)";

    private static final String SELECT =
            "SELECT p.cd_paciente, p.nm_paciente, p.nr_cns, p.dt_nascimento, p.nr_telefone, p.nm_bairro, "
                    + "p.ds_canal_contato, p.nr_telefone_familiar, "
                    + "CASE WHEN (SELECT COUNT(*) FROM tb_consulta c WHERE c.cd_paciente = p.cd_paciente "
                    + "AND c.ds_status IN ('REALIZADA', 'FALTA')) = 0 THEN 1 ELSE 0 END AS primeira_consulta, "
                    + "(SELECT MIN(c.dt_consulta) " + FUTURAS + ") AS proxima_data, "
                    + "(SELECT MIN(c.cd_consulta) KEEP (DENSE_RANK FIRST ORDER BY c.dt_consulta) " + FUTURAS + ") AS proxima_id "
                    + "FROM tb_paciente p";

    private final DataSource dataSource;

    public PacienteDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Paciente> listar(String busca, String cartaoSus) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (busca != null && !busca.isBlank()) {
            sql.append(" AND UPPER(p.nm_paciente) LIKE ?");
            parametros.add("%" + busca.trim().toUpperCase() + "%");
        }
        if (cartaoSus != null && !cartaoSus.isBlank()) {
            sql.append(" AND p.nr_cns = ?");
            parametros.add(cartaoSus.trim());
        }
        sql.append(" ORDER BY p.nm_paciente");

        List<Paciente> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            JdbcUtil.setParametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(paciente(rs));
                }
            }
        }
        return lista;
    }

    public Optional<Paciente> buscarPorId(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE p.cd_paciente = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(paciente(rs)) : Optional.empty();
            }
        }
    }

    private static Paciente paciente(ResultSet rs) throws SQLException {
        Paciente paciente = JdbcUtil.paciente(rs);
        paciente.setPrimeiraConsulta(rs.getInt("primeira_consulta") == 1);
        paciente.setProximaConsulta(JdbcUtil.dataHora(rs, "proxima_data"));
        paciente.setProximaConsultaId(JdbcUtil.longOuNulo(rs, "proxima_id"));
        return paciente;
    }

    public Long inserir(Paciente p) throws SQLException {
        String sql = "INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, "
                + "ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"CD_PACIENTE"})) {
            preencher(ps, p);
            ps.executeUpdate();
            return UnidadeDao.idGerado(ps);
        }
    }

    public boolean atualizar(Paciente p) throws SQLException {
        String sql = "UPDATE tb_paciente SET nm_paciente = ?, nr_cns = ?, dt_nascimento = ?, nr_telefone = ?, "
                + "nm_bairro = ?, ds_canal_contato = ?, nr_telefone_familiar = ? WHERE cd_paciente = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            preencher(ps, p);
            ps.setLong(8, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean remover(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_paciente WHERE cd_paciente = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static void preencher(PreparedStatement ps, Paciente p) throws SQLException {
        ps.setString(1, p.getNome());
        ps.setString(2, p.getCartaoSus());
        JdbcUtil.setData(ps, 3, p.getDataNascimento());
        ps.setString(4, p.getTelefone());
        ps.setString(5, p.getBairro());
        ps.setString(6, p.getCanalContato());
        ps.setString(7, p.getTelefoneFamiliar());
    }
}
