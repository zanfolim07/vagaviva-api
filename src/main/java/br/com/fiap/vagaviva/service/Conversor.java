package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.dto.ConsultaResponse;
import br.com.fiap.vagaviva.dto.EspecialidadeResponse;
import br.com.fiap.vagaviva.dto.ListaEsperaResponse;
import br.com.fiap.vagaviva.dto.OfertaResponse;
import br.com.fiap.vagaviva.dto.PacienteResponse;
import br.com.fiap.vagaviva.dto.PacienteConsultaResumo;
import br.com.fiap.vagaviva.dto.PacienteResumo;
import br.com.fiap.vagaviva.dto.ProfissionalResponse;
import br.com.fiap.vagaviva.dto.ProfissionalConsultaResumo;
import br.com.fiap.vagaviva.dto.ProfissionalResumo;
import br.com.fiap.vagaviva.dto.RiscoResponse;
import br.com.fiap.vagaviva.dto.UnidadeResponse;
import br.com.fiap.vagaviva.dto.UnidadeResumo;
import br.com.fiap.vagaviva.dto.UsuarioResponse;
import br.com.fiap.vagaviva.model.Consulta;
import br.com.fiap.vagaviva.model.Especialidade;
import br.com.fiap.vagaviva.model.ListaEspera;
import br.com.fiap.vagaviva.model.Paciente;
import br.com.fiap.vagaviva.model.Profissional;
import br.com.fiap.vagaviva.model.Unidade;
import br.com.fiap.vagaviva.model.Usuario;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class Conversor {

    private final ModelMapper mapper;

    public Conversor(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T copiar(Object origem, Class<T> destino) {
        return origem == null ? null : mapper.map(origem, destino);
    }

    public UnidadeResponse unidade(Unidade u) {
        return copiar(u, UnidadeResponse.class);
    }

    public UsuarioResponse usuario(Usuario u) {
        UsuarioResponse r = copiar(u, UsuarioResponse.class);
        r.setUnidade(copiar(u.getUnidade(), UnidadeResumo.class));
        return r;
    }

    public EspecialidadeResponse especialidade(Especialidade e) {
        return copiar(e, EspecialidadeResponse.class);
    }

    public ProfissionalResponse profissional(Profissional p) {
        ProfissionalResponse r = copiar(p, ProfissionalResponse.class);
        r.setUnidade(copiar(p.getUnidade(), UnidadeResumo.class));
        r.setEspecialidades(p.getEspecialidades().stream().map(this::especialidade).toList());
        return r;
    }

    public PacienteResponse paciente(Paciente p) {
        PacienteResponse r = copiar(p, PacienteResponse.class);
        r.setPrimeiraConsulta(p.isPrimeiraConsulta());
        return r;
    }

    public ConsultaResponse consulta(Consulta c) {
        ConsultaResponse r = new ConsultaResponse();
        r.setId(c.getId());
        r.setDataConsulta(c.getDataConsulta());
        r.setTokenPaciente(c.getToken());
        r.setStatus(c.getStatus());
        PacienteConsultaResumo paciente = new PacienteConsultaResumo();
        paciente.setId(c.getPaciente().getId());
        paciente.setNome(c.getPaciente().getNome());
        paciente.setCanalContato(c.getPaciente().getCanalContato());
        paciente.setPrimeiraConsulta(c.isPrimeiraConsulta());
        r.setPaciente(paciente);

        ProfissionalConsultaResumo profissional = new ProfissionalConsultaResumo();
        profissional.setId(c.getProfissional().getId());
        profissional.setNome(c.getProfissional().getNome());
        profissional.setEspecialidade(c.getEspecialidade() == null ? null : c.getEspecialidade().getNome());
        r.setProfissional(profissional);
        r.setUnidade(copiar(c.getUnidade(), UnidadeResumo.class));
        r.setDataAgendamento(c.getDataAgendamento() == null ? null : c.getDataAgendamento().toLocalDate());
        r.setEnviarLembrete(c.isLembrete());

        RiscoResponse risco = new RiscoResponse();
        risco.setValor(c.getRiscoFalta());
        risco.setNivel(c.getNivelRisco());
        risco.setMotivos(motivos(c));
        r.setRisco(risco);
        r.setAcaoSugerida(acaoSugerida(c));
        return r;
    }

    private static List<String> motivos(Consulta c) {
        if (c.getRiscoFalta() == null || c.getMotivoRisco() == null
                || "NAO_CALCULADO".equals(c.getNivelRisco())) {
            return List.of();
        }
        return Arrays.stream(c.getMotivoRisco().split("[;,]\\s*"))
                .filter(motivo -> !motivo.isBlank())
                .toList();
    }

    private static String acaoSugerida(Consulta c) {
        String canalContato = c.getPaciente().getCanalContato();
        String canal = canalContato == null ? "canal preferido" : switch (canalContato) {
            case "WHATSAPP" -> "WhatsApp";
            case "SMS" -> "SMS";
            case "LIGACAO" -> "ligação";
            default -> "canal preferido";
        };
        if ("ALTO".equals(c.getNivelRisco())) {
            return "Contato prioritário por " + canal + "; sem resposta até 48h antes, ligação";
        }
        if ("MEDIO".equals(c.getNivelRisco())) {
            return "Enviar lembrete por " + canal + " 48h antes da consulta";
        }
        if ("BAIXO".equals(c.getNivelRisco())) {
            return "Manter confirmação pelo canal " + canal;
        }
        return "Risco não calculado; acompanhar pelo canal preferido";
    }

    public OfertaResponse oferta(ListaEspera e) {
        if (e == null) {
            return null;
        }
        OfertaResponse r = new OfertaResponse();
        r.setEsperaId(e.getId());
        r.setPaciente(copiar(e.getPaciente(), PacienteResumo.class));
        r.setClasseRisco(e.getClasseRisco());
        r.setPrazoLegal(e.isPrazoLegal());
        r.setPontuacao(e.pontuacao(Relogio.hoje()));
        r.setConsultaId(e.getConsultaOfertadaId());
        r.setDataConsulta(e.getDataConsultaOfertada());
        r.setRespondaAte(e.getLimiteOferta());
        return r;
    }

    public ListaEsperaResponse listaEspera(ListaEspera e, Integer posicao, boolean comJustificativa) {
        LocalDate hoje = Relogio.hoje();
        ListaEsperaResponse r = new ListaEsperaResponse();
        r.setId(e.getId());
        r.setPosicao(posicao);
        r.setTokenPaciente(e.getToken());
        r.setPaciente(copiar(e.getPaciente(), PacienteResumo.class));
        r.setEspecialidade(especialidade(e.getEspecialidade()));
        r.setUnidade(copiar(e.getUnidade(), UnidadeResumo.class));
        r.setSolicitante(copiar(e.getSolicitante(), ProfissionalResumo.class));
        r.setProfissionalPreferido(copiar(e.getProfissionalPreferido(), ProfissionalResumo.class));
        r.setDataEntrada(e.getDataEntrada());
        r.setDiasEspera(e.diasEspera(hoje));
        r.setPrazoDias(e.prazoDias());
        r.setDataLimite(e.dataLimite());
        r.setPrazoVencido(e.naFila() && e.prazoVencido(hoje));
        r.setPontuacao(e.pontuacao(hoje));
        r.setClasseRisco(e.getClasseRisco());
        r.setPrazoLegal(e.isPrazoLegal());
        r.setPrioridadeLegal(e.getPrioridadeLegal());
        r.setAceitaMesmoDia(e.isAceitaMesmoDia());
        r.setStatus(e.getStatus());
        r.setConsultaOfertadaId(e.getConsultaOfertadaId());
        r.setDataConsultaOfertada(e.getDataConsultaOfertada());
        r.setRespondaAte(e.getLimiteOferta());
        r.setJustificativaClinica(comJustificativa ? e.getJustificativaClinica() : null);
        return r;
    }
}
