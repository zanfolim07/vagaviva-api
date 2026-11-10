package br.com.fiap.vagaviva.dao;

import br.com.fiap.vagaviva.model.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDao {

    private static final String SELECT =
            "SELECT us.cd_usuario, us.nm_usuario, us.ds_email, us.ds_senha_hash, us.ds_perfil, us.ds_status, "
                    + "us.ds_codigo_convite, us.dt_expiracao_convite, us.dt_ultimo_acesso, "
                    + "u.cd_unidade, u.nm_unidade, u.ds_endereco, u.nm_bairro, u.nm_cidade, u.nr_telefone "
                    + "FROM tb_usuario us INNER JOIN tb_unidade u ON u.cd_unidade = us.cd_unidade";

    private final DataSource dataSource;

    public UsuarioDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Usuario> listar(Long unidadeId) throws SQLException {
        String sql = SELECT + " WHERE (? IS NULL OR us.cd_unidade = ?) ORDER BY us.nm_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setLong(ps, 1, unidadeId);
            JdbcUtil.setLong(ps, 2, unidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(usuario(rs));
                }
            }
        }
        return lista;
    }

    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        return buscarUm(SELECT + " WHERE us.cd_usuario = ?", id);
    }

    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        return buscarUm(SELECT + " WHERE LOWER(us.ds_email) = ?", email);
    }

    public Optional<Usuario> buscarPorConvite(String codigo) throws SQLException {
        return buscarUm(SELECT + " WHERE us.ds_codigo_convite = ?", codigo);
    }

    private Optional<Usuario> buscarUm(String sql, Object parametro) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(usuario(rs)) : Optional.empty();
            }
        }
    }

    public Long inserir(Usuario u) throws SQLException {
        String sql = "INSERT INTO tb_usuario (cd_usuario, cd_unidade, nm_usuario, ds_email, ds_senha_hash, ds_perfil, "
                + "ds_status, ds_codigo_convite, dt_expiracao_convite) "
                + "VALUES (sq_tb_usuario.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"CD_USUARIO"})) {
            preencher(ps, u);
            ps.executeUpdate();
            return UnidadeDao.idGerado(ps);
        }
    }

    public boolean atualizar(Usuario u) throws SQLException {
        String sql = "UPDATE tb_usuario SET cd_unidade = ?, nm_usuario = ?, ds_email = ?, ds_senha_hash = ?, "
                + "ds_perfil = ?, ds_status = ?, ds_codigo_convite = ?, dt_expiracao_convite = ? WHERE cd_usuario = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            preencher(ps, u);
            ps.setLong(9, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public void registrarAcesso(Long id, LocalDateTime quando) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE tb_usuario SET dt_ultimo_acesso = ? WHERE cd_usuario = ?")) {
            JdbcUtil.setDataHora(ps, 1, quando);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public boolean remover(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_usuario WHERE cd_usuario = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static void preencher(PreparedStatement ps, Usuario u) throws SQLException {
        ps.setLong(1, u.getUnidade().getId());
        ps.setString(2, u.getNome());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getSenhaHash());
        ps.setString(5, u.getPerfil());
        ps.setString(6, u.getStatus());
        ps.setString(7, u.getCodigoConvite());
        JdbcUtil.setData(ps, 8, u.getExpiracaoConvite());
    }

    private static Usuario usuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("cd_usuario"));
        u.setNome(rs.getString("nm_usuario"));
        u.setEmail(rs.getString("ds_email"));
        u.setSenhaHash(rs.getString("ds_senha_hash"));
        u.setPerfil(rs.getString("ds_perfil"));
        u.setStatus(rs.getString("ds_status"));
        u.setCodigoConvite(rs.getString("ds_codigo_convite"));
        u.setExpiracaoConvite(JdbcUtil.data(rs, "dt_expiracao_convite"));
        u.setUltimoAcesso(JdbcUtil.dataHora(rs, "dt_ultimo_acesso"));
        u.setUnidade(JdbcUtil.unidade(rs, false));
        return u;
    }
}
