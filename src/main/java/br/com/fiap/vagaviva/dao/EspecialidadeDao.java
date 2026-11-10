package br.com.fiap.vagaviva.dao;

import br.com.fiap.vagaviva.model.Especialidade;
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
public class EspecialidadeDao {

    private final DataSource dataSource;

    public EspecialidadeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Especialidade> listar() throws SQLException {
        List<Especialidade> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT cd_especialidade, nm_especialidade FROM tb_especialidade ORDER BY nm_especialidade");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(especialidade(rs));
            }
        }
        return lista;
    }

    public Optional<Especialidade> buscarPorId(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT cd_especialidade, nm_especialidade FROM tb_especialidade WHERE cd_especialidade = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(especialidade(rs)) : Optional.empty();
            }
        }
    }

    public Long inserir(Especialidade e) throws SQLException {
        String sql = "INSERT INTO tb_especialidade (cd_especialidade, nm_especialidade) VALUES (sq_tb_especialidade.NEXTVAL, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"CD_ESPECIALIDADE"})) {
            ps.setString(1, e.getNome());
            ps.executeUpdate();
            return UnidadeDao.idGerado(ps);
        }
    }

    public boolean atualizar(Especialidade e) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE tb_especialidade SET nm_especialidade = ? WHERE cd_especialidade = ?")) {
            ps.setString(1, e.getNome());
            ps.setLong(2, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean remover(Long id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_especialidade WHERE cd_especialidade = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Especialidade especialidade(ResultSet rs) throws SQLException {
        Especialidade e = new Especialidade();
        e.setId(rs.getLong("cd_especialidade"));
        e.setNome(rs.getString("nm_especialidade"));
        return e;
    }
}
