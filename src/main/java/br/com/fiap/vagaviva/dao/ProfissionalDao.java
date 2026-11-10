package br.com.fiap.vagaviva.dao;

import br.com.fiap.vagaviva.model.Especialidade;
import br.com.fiap.vagaviva.model.Profissional;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProfissionalDao {

    private static final String SELECT =
            "SELECT pr.cd_profissional, pr.nm_profissional, pr.nr_registro, pr.ds_categoria, "
                    + "u.cd_unidade, u.nm_unidade, u.ds_endereco, u.nm_bairro, u.nm_cidade, u.nr_telefone "
                    + "FROM tb_profissional pr "
                    + "INNER JOIN tb_unidade u ON u.cd_unidade = pr.cd_unidade";

    private final DataSource dataSource;

    public ProfissionalDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Profissional> listar(Long unidadeId, Long especialidadeId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (unidadeId != null) {
            sql.append(" AND pr.cd_unidade = ?");
            parametros.add(unidadeId);
        }
        if (especialidadeId != null) {
            sql.append(" AND pr.cd_profissional IN (SELECT cd_profissional FROM tb_profissional_especialidade "
                    + "WHERE cd_especialidade = ?)");
            parametros.add(especialidadeId);
        }
        sql.append(" ORDER BY pr.nm_profissional");

        Map<Long, Profissional> mapa = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                JdbcUtil.setParametros(ps, parametros);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Profissional p = profissional(rs);
                        mapa.put(p.getId(), p);
                    }
                }
            }
            carregarEspecialidades(conn, mapa);
        }
        return new ArrayList<>(mapa.values());
    }

    public Optional<Profissional> buscarPorId(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return buscarPorId(conn, id);
        }
    }

    public Optional<Profissional> buscarPorId(Connection conn, Long id) throws SQLException {
        Profissional p = null;
        try (PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE pr.cd_profissional = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = profissional(rs);
                }
            }
        }
        if (p == null) {
            return Optional.empty();
        }
        Map<Long, Profissional> mapa = new LinkedHashMap<>();
        mapa.put(p.getId(), p);
        carregarEspecialidades(conn, mapa);
        return Optional.of(p);
    }

    public boolean atendeEspecialidade(Long profissionalId, Long especialidadeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_profissional_especialidade WHERE cd_profissional = ? AND cd_especialidade = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, profissionalId);
            ps.setLong(2, especialidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Long inserir(Connection conn, Profissional p) throws SQLException {
        String sql = "INSERT INTO tb_profissional (cd_profissional, cd_unidade, nm_profissional, nr_registro, ds_categoria) "
                + "VALUES (sq_tb_profissional.NEXTVAL, ?, ?, ?, ?)";
        Long id;
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"CD_PROFISSIONAL"})) {
            ps.setLong(1, p.getUnidade().getId());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getRegistro());
            ps.setString(4, p.getCategoria());
            ps.executeUpdate();
            id = UnidadeDao.idGerado(ps);
        }
        inserirEspecialidades(conn, id, p.getEspecialidades());
        return id;
    }

    public boolean atualizar(Connection conn, Profissional p) throws SQLException {
        String sql = "UPDATE tb_profissional SET cd_unidade = ?, nm_profissional = ?, nr_registro = ?, ds_categoria = ? "
                + "WHERE cd_profissional = ?";
        int linhas;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, p.getUnidade().getId());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getRegistro());
            ps.setString(4, p.getCategoria());
            ps.setLong(5, p.getId());
            linhas = ps.executeUpdate();
        }
        if (linhas == 0) {
            return false;
        }
        removerEspecialidades(conn, p.getId());
        inserirEspecialidades(conn, p.getId(), p.getEspecialidades());
        return true;
    }

    public boolean remover(Connection conn, Long id) throws SQLException {
        removerEspecialidades(conn, id);
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_profissional WHERE cd_profissional = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void inserirEspecialidades(Connection conn, Long profissionalId, List<Especialidade> especialidades)
            throws SQLException {
        String sql = "INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Especialidade e : especialidades) {
                ps.setLong(1, profissionalId);
                ps.setLong(2, e.getId());
                ps.executeUpdate();
            }
        }
    }

    private void removerEspecialidades(Connection conn, Long profissionalId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tb_profissional_especialidade WHERE cd_profissional = ?")) {
            ps.setLong(1, profissionalId);
            ps.executeUpdate();
        }
    }

    private void carregarEspecialidades(Connection conn, Map<Long, Profissional> mapa) throws SQLException {
        if (mapa.isEmpty()) {
            return;
        }
        String sql = "SELECT pe.cd_profissional, e.cd_especialidade, e.nm_especialidade "
                + "FROM tb_profissional_especialidade pe "
                + "INNER JOIN tb_especialidade e ON e.cd_especialidade = pe.cd_especialidade "
                + "ORDER BY e.nm_especialidade";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Profissional p = mapa.get(rs.getLong("cd_profissional"));
                if (p != null) {
                    Especialidade e = new Especialidade();
                    e.setId(rs.getLong("cd_especialidade"));
                    e.setNome(rs.getString("nm_especialidade"));
                    p.getEspecialidades().add(e);
                }
            }
        }
    }

    private static Profissional profissional(ResultSet rs) throws SQLException {
        Profissional p = new Profissional();
        p.setId(rs.getLong("cd_profissional"));
        p.setNome(rs.getString("nm_profissional"));
        p.setRegistro(rs.getString("nr_registro"));
        p.setCategoria(rs.getString("ds_categoria"));
        p.setUnidade(JdbcUtil.unidade(rs, false));
        return p;
    }
}
