package br.com.fiap.vagaviva.dao;

import br.com.fiap.vagaviva.model.Unidade;
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
public class UnidadeDao {

    private static final String SELECT =
            "SELECT cd_unidade, nm_unidade, ds_endereco, nm_bairro, nm_cidade, nr_telefone FROM tb_unidade";

    private final DataSource dataSource;

    public UnidadeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Unidade> listar() throws SQLException {
        List<Unidade> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY nm_unidade");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(JdbcUtil.unidade(rs, false));
            }
        }
        return lista;
    }

    public Optional<Unidade> buscarPorId(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE cd_unidade = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(JdbcUtil.unidade(rs, false)) : Optional.empty();
            }
        }
    }

    public Long inserir(Unidade u) throws SQLException {
        String sql = "INSERT INTO tb_unidade (cd_unidade, nm_unidade, ds_endereco, nm_bairro, nm_cidade, nr_telefone) "
                + "VALUES (sq_tb_unidade.NEXTVAL, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"CD_UNIDADE"})) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEndereco());
            ps.setString(3, u.getBairro());
            ps.setString(4, u.getCidade());
            ps.setString(5, u.getTelefone());
            ps.executeUpdate();
            return idGerado(ps);
        }
    }

    public boolean atualizar(Unidade u) throws SQLException {
        String sql = "UPDATE tb_unidade SET nm_unidade = ?, ds_endereco = ?, nm_bairro = ?, nm_cidade = ?, nr_telefone = ? "
                + "WHERE cd_unidade = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEndereco());
            ps.setString(3, u.getBairro());
            ps.setString(4, u.getCidade());
            ps.setString(5, u.getTelefone());
            ps.setLong(6, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean remover(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_unidade WHERE cd_unidade = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    static Long idGerado(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getLong(1);
            }
        }
        throw new SQLException("O banco não devolveu o id gerado pela sequence.");
    }
}
