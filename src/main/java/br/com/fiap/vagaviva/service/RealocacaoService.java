package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.dao.ListaEsperaDao;
import br.com.fiap.vagaviva.model.Consulta;
import br.com.fiap.vagaviva.model.FilaPrioridade;
import br.com.fiap.vagaviva.model.ListaEspera;
import br.com.fiap.vagaviva.model.PrazoOferta;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RealocacaoService {

    private final ListaEsperaDao listaEsperaDao;

    public RealocacaoService(ListaEsperaDao listaEsperaDao) {
        this.listaEsperaDao = listaEsperaDao;
    }

    public ListaEspera oferecerVaga(Connection conn, Consulta vaga, Long ignorarEsperaId) throws SQLException {
        LocalDateTime agora = Relogio.agora();
        boolean somenteMesmoDia = vaga.faltamMenosDe24h(agora);
        LocalDateTime limite = PrazoOferta.limite(agora, vaga.getDataConsulta());
        List<ListaEspera> candidatos = listaEsperaDao.listarCandidatos(conn, vaga.getId(),
                vaga.getUnidade().getId(), vaga.getProfissional().getId(), somenteMesmoDia,
                ignorarEsperaId, vaga.getPaciente().getId());
        candidatos.sort(FilaPrioridade.ordem(Relogio.hoje()));

        for (ListaEspera candidato : candidatos) {
            if (listaEsperaDao.ofertar(conn, candidato.getId(), vaga.getId(), limite)) {
                candidato.setStatus(ListaEspera.OFERTADA);
                candidato.setConsultaOfertadaId(vaga.getId());
                candidato.setDataConsultaOfertada(vaga.getDataConsulta());
                candidato.setLimiteOferta(limite);
                return candidato;
            }
        }
        return null;
    }
}
