package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.dao.ConsultaDao;
import br.com.fiap.vagaviva.dto.AcaoConsultaResponse;
import br.com.fiap.vagaviva.dto.ConsultaRequest;
import br.com.fiap.vagaviva.dto.ConsultaResponse;
import br.com.fiap.vagaviva.exception.HorarioOcupadoException;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.exception.RegraNegocioException;
import br.com.fiap.vagaviva.model.Consulta;
import br.com.fiap.vagaviva.model.Especialidade;
import br.com.fiap.vagaviva.model.ListaEspera;
import br.com.fiap.vagaviva.model.Paciente;
import br.com.fiap.vagaviva.model.Profissional;
import br.com.fiap.vagaviva.model.StatusConsulta;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import br.com.fiap.vagaviva.dto.ExplicacaoRiscoResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Service
public class ConsultaService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");
    private static final List<LocalTime> HORARIOS_ATENDIMENTO = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 30), LocalTime.of(14, 0), LocalTime.of(15, 30));

    private final ConsultaDao dao;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;
    private final RiscoService riscoService;
    private final RealocacaoService realocacaoService;
    private final Transacao transacao;
    private final Conversor conversor;

    public ConsultaService(ConsultaDao dao, PacienteService pacienteService, ProfissionalService profissionalService,
                           RiscoService riscoService, RealocacaoService realocacaoService, Transacao transacao,
                           Conversor conversor) {
        this.dao = dao;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
        this.riscoService = riscoService;
        this.realocacaoService = realocacaoService;
        this.transacao = transacao;
        this.conversor = conversor;
    }

    public List<ConsultaResponse> listar(LocalDate data, Long unidadeId, Long profissionalId, StatusConsulta status)
            throws SQLException {
        return dao.listar(data, unidadeId, profissionalId, null, status).stream().map(conversor::consulta).toList();
    }

    public List<ConsultaResponse> listarDoPaciente(Long pacienteId) throws SQLException {
        pacienteService.obter(pacienteId);
        return dao.listar(null, null, null, pacienteId, null).stream().map(conversor::consulta).toList();
    }

    public List<ConsultaResponse> listarDoProfissional(Long profissionalId, LocalDate data) throws SQLException {
        profissionalService.obter(profissionalId);
        return dao.listar(data, null, profissionalId, null, null).stream().map(conversor::consulta).toList();
    }

    public List<String> horariosLivres(Long profissionalId, LocalDate data) throws SQLException {
        profissionalService.obter(profissionalId);
        Set<LocalTime> ocupados = Set.copyOf(dao.horariosOcupados(profissionalId, data));
        return HORARIOS_ATENDIMENTO.stream()
                .filter(horario -> !ocupados.contains(horario))
                .map(horario -> horario.format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();
    }

    public ConsultaResponse buscar(Long id) throws SQLException {
        return conversor.consulta(obter(id));
    }

    public ConsultaResponse agendar(Long pacienteId, ConsultaRequest req) throws SQLException {
        Paciente paciente = pacienteService.obter(pacienteId);
        Profissional profissional = profissionalService.obter(req.getProfissionalId());
        Especialidade especialidade = especialidadeDaConsulta(profissional, req.getEspecialidadeId());
        LocalDateTime dataConsulta = normalizar(req.getDataConsulta());
        conferirHorario(profissional, dataConsulta, null);

        Consulta c = new Consulta();
        c.setPaciente(paciente);
        c.setProfissional(profissional);
        c.setEspecialidade(especialidade);
        c.setDataAgendamento(Relogio.agora());
        c.setDataConsulta(dataConsulta);
        c.setStatus(StatusConsulta.AGENDADA);
        c.setToken(novoToken());

        int[] historico = riscoService.historico(pacienteId);
        c.setLembrete(riscoService.lembreteObrigatorio(historico) || req.getLembrete() == null || req.getLembrete());
        riscoService.calcular(c, historico);

        Long id = dao.inserir(c);
        return buscar(id);
    }

    public ConsultaResponse remarcar(Long id, ConsultaRequest req) throws SQLException {
        Consulta c = obter(id);
        if (!c.getStatus().emAberto()) {
            throw new RegraNegocioException("A consulta está " + c.getStatus() + " e não pode ser remarcada.");
        }
        Profissional profissional = profissionalService.obter(req.getProfissionalId());
        Especialidade especialidade = especialidadeDaConsulta(profissional, req.getEspecialidadeId());
        LocalDateTime dataConsulta = normalizar(req.getDataConsulta());
        conferirHorario(profissional, dataConsulta, id);

        c.setProfissional(profissional);
        c.setEspecialidade(especialidade);
        c.setDataConsulta(dataConsulta);
        c.setDataAgendamento(Relogio.agora());
        c.setStatus(StatusConsulta.AGENDADA);
        int[] historico = riscoService.historico(c.getPaciente().getId());
        c.setLembrete(riscoService.lembreteObrigatorio(historico) || req.getLembrete() == null || req.getLembrete());
        riscoService.calcular(c, historico);

        dao.atualizar(c);
        return buscar(id);
    }

    public void remover(Long id) throws SQLException {
        if (!dao.remover(id)) {
            throw naoEncontrada(id);
        }
    }

    public ExplicacaoRiscoResponse explicarRisco(Long id) throws SQLException {
        Consulta c = obter(id);
        if (Consulta.NIVEL_NAO_CALCULADO.equals(c.getNivelRisco())) {
            return new ExplicacaoRiscoResponse(false, List.of());
        }
        return riscoService.explicar(c);
    }

    public ConsultaResponse confirmar(Long id) throws SQLException {
        Consulta c = obter(id);
        if (c.getStatus() == StatusConsulta.CONFIRMADA) {
            return conversor.consulta(c);
        }
        if (c.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("A consulta está " + c.getStatus() + " e não pode ser confirmada.");
        }
        if (c.jaPassou(Relogio.agora())) {
            throw new RegraNegocioException("O horário da consulta já passou. Registre a presença ou a falta.");
        }
        dao.atualizarStatus(id, StatusConsulta.CONFIRMADA);
        return buscar(id);
    }

    public AcaoConsultaResponse cancelar(Long id) throws SQLException {
        Consulta c = obter(id);
        if (!c.getStatus().emAberto()) {
            throw new RegraNegocioException("A consulta está " + c.getStatus() + " e não pode ser cancelada.");
        }
        if (c.jaPassou(Relogio.agora())) {
            throw new RegraNegocioException("O horário da consulta já passou. Registre a presença ou a falta.");
        }
        return devolverVaga(id, StatusConsulta.CANCELADA);
    }

    public AcaoConsultaResponse liberar(Long id) throws SQLException {
        Consulta c = obter(id);
        LocalDateTime agora = Relogio.agora();
        if (c.getStatus() == StatusConsulta.CONFIRMADA) {
            throw new RegraNegocioException("O paciente confirmou presença. Para desmarcar, use o cancelamento.");
        }
        if (c.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("A consulta está " + c.getStatus() + " e não pode ser liberada.");
        }
        if (c.jaPassou(agora)) {
            throw new RegraNegocioException("O horário da consulta já passou. Registre a presença ou a falta.");
        }
        if (!c.faltamMenosDe24h(agora)) {
            throw new RegraNegocioException("A recepção só pode liberar a vaga a menos de 24 horas da consulta, "
                    + "quando o paciente não respondeu (RN05).");
        }
        return devolverVaga(id, StatusConsulta.LIBERADA);
    }

    public ConsultaResponse registrarPresenca(Long id, boolean compareceu) throws SQLException {
        Consulta c = obter(id);
        if (!c.getStatus().emAberto()) {
            throw new RegraNegocioException("A consulta está " + c.getStatus() + " e não aceita registro de presença.");
        }
        if (c.getDataConsulta().toLocalDate().isAfter(Relogio.hoje())) {
            throw new RegraNegocioException("Só é possível registrar presença no dia da consulta ou depois.");
        }
        dao.atualizarStatus(id, compareceu ? StatusConsulta.REALIZADA : StatusConsulta.FALTA);
        return buscar(id);
    }

    private AcaoConsultaResponse devolverVaga(Long id, StatusConsulta novoStatus) throws SQLException {
        return transacao.executar(conn -> {
            dao.travar(conn, id);
            Consulta c = dao.buscarPorId(conn, id).orElseThrow(() -> naoEncontrada(id));
            if (!c.getStatus().emAberto()) {
                throw new RegraNegocioException("A consulta acabou de ser alterada e está " + c.getStatus() + ".");
            }
            dao.atualizarStatus(conn, id, novoStatus);
            c.setStatus(novoStatus);
            ListaEspera oferta = realocacaoService.oferecerVaga(conn, c, null);

            AcaoConsultaResponse resposta = new AcaoConsultaResponse();
            resposta.setConsulta(conversor.consulta(c));
            resposta.setOferta(conversor.oferta(oferta));
            return resposta;
        });
    }

    public Consulta obter(Long id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> naoEncontrada(id));
    }

    private void conferirHorario(Profissional profissional, LocalDateTime dataConsulta, Long ignorarConsultaId)
            throws SQLException {
        if (!HORARIOS_ATENDIMENTO.contains(dataConsulta.toLocalTime())) {
            throw new RegraNegocioException("Horário fora da grade de atendimento. Use 09:00, 10:30, 14:00 ou 15:30.");
        }
        if (dao.horarioOcupado(profissional.getId(), dataConsulta, ignorarConsultaId)) {
            throw new HorarioOcupadoException(profissional.getNome() + " já tem consulta em "
                    + dataConsulta.format(DATA_HORA) + ".");
        }
    }

    private static LocalDateTime normalizar(LocalDateTime data) {
        return data.withSecond(0).withNano(0);
    }

    static String novoToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static RecursoNaoEncontradoException naoEncontrada(Long id) {
        return new RecursoNaoEncontradoException("Consulta " + id + " não encontrada.");
    }

    private static Especialidade especialidadeDaConsulta(Profissional profissional, Long especialidadeId) {
        if (profissional.getEspecialidades() == null || profissional.getEspecialidades().isEmpty()) {
            throw new RegraNegocioException("O profissional não tem especialidade cadastrada.");
        }
        if (especialidadeId == null) {
            return profissional.getEspecialidades().get(0);
        }
        return profissional.getEspecialidades().stream()
                .filter(especialidade -> especialidade.getId().equals(especialidadeId))
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("O profissional não atende a especialidade informada."));
    }
}
