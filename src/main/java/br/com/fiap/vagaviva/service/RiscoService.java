package br.com.fiap.vagaviva.service;

import br.com.fiap.vagaviva.Relogio;
import br.com.fiap.vagaviva.client.IaClient;
import br.com.fiap.vagaviva.client.IaRequest;
import br.com.fiap.vagaviva.client.IaResponse;
import br.com.fiap.vagaviva.dao.ConsultaDao;
import br.com.fiap.vagaviva.dto.ExplicacaoRiscoResponse;
import br.com.fiap.vagaviva.dto.FatorRiscoResponse;
import br.com.fiap.vagaviva.model.Consulta;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RiscoService {

    private static final int TAMANHO_MOTIVO = 200;
    private static final int DIAS_REFERENCIA = 7;
    private static final int IDADE_REFERENCIA = 37;
    private static final int DIA_REFERENCIA = 2;
    private static final String[] NOMES_DIAS = {"Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo"};

    private final ConsultaDao consultaDao;
    private final IaClient iaClient;

    public RiscoService(ConsultaDao consultaDao, IaClient iaClient) {
        this.consultaDao = consultaDao;
        this.iaClient = iaClient;
    }

    public int[] historico(Long pacienteId) throws SQLException {
        return consultaDao.historico(pacienteId, Relogio.agora());
    }

    public boolean lembreteObrigatorio(int[] historico) {
        return historico[0] == 0;
    }

    public void calcular(Consulta c, int[] historico) {
        LocalDateTime agora = Relogio.agora();
        int total = historico[0];
        int faltas = historico[1];
        long dias = Math.max(0, ChronoUnit.DAYS.between(agora.toLocalDate(), c.getDataConsulta().toLocalDate()));
        int idade = c.getPaciente().idadeEm(c.getDataConsulta().toLocalDate());
        int diaSemana = c.getDataConsulta().getDayOfWeek().getValue() - 1;

        Optional<IaResponse> resposta = iaClient.preverFalta(entrada(idade, dias, diaSemana, c.isLembrete(), total, faltas));
        if (resposta.isPresent()) {
            IaResponse ia = resposta.get();
            c.setRiscoFalta(Math.round(ia.getProbabilidadeFalta() * 1000) / 1000.0);
            c.setNivelRisco(ia.getNivel());
            c.setMotivoRisco(motivo(ia));
        } else {
            c.setRiscoFalta(null);
            c.setNivelRisco(Consulta.NIVEL_NAO_CALCULADO);
            c.setMotivoRisco("Risco não calculado: serviço de IA indisponível no agendamento.");
        }
    }

    public ExplicacaoRiscoResponse explicar(Consulta c) throws SQLException {
        int[] historico = consultaDao.historico(c.getPaciente().getId(), c.getDataAgendamento());
        int total = historico[0];
        int faltas = historico[1];
        long dias = Math.max(0, ChronoUnit.DAYS.between(c.getDataAgendamento().toLocalDate(), c.getDataConsulta().toLocalDate()));
        int idade = c.getPaciente().idadeEm(c.getDataConsulta().toLocalDate());
        int dia = c.getDataConsulta().getDayOfWeek().getValue() - 1;
        boolean lembrete = c.isLembrete();

        Optional<IaResponse> base = iaClient.preverFalta(entrada(idade, dias, dia, lembrete, total, faltas));
        if (base.isEmpty()) {
            return new ExplicacaoRiscoResponse(false, List.of());
        }
        double atual = base.get().getProbabilidadeFalta();

        String textoDias = dias == 0 ? "Marcada para o mesmo dia"
                : "Marcada com " + dias + (dias == 1 ? " dia" : " dias") + " de antecedência";
        String textoHistorico;
        String comparacaoHistorico;
        IaRequest semEsseHistorico;
        if (total == 0) {
            textoHistorico = "Primeira consulta, sem histórico";
            comparacaoHistorico = "em relação a quem já compareceu a uma consulta";
            semEsseHistorico = entrada(idade, dias, dia, lembrete, 1, 0);
        } else if (faltas > 0) {
            textoHistorico = "Faltou em " + faltas + " de " + total + (total == 1 ? " consulta" : " consultas");
            comparacaoHistorico = "em relação a não ter faltado";
            semEsseHistorico = entrada(idade, dias, dia, lembrete, total, 0);
        } else {
            textoHistorico = "Compareceu " + (total == 1 ? "à consulta anterior" : "às " + total + " consultas anteriores");
            comparacaoHistorico = "em relação a uma primeira consulta";
            semEsseHistorico = entrada(idade, dias, dia, lembrete, 0, 0);
        }

        List<FatorRiscoResponse> fatores = new ArrayList<>(List.of(
                fator("Antecedência", textoDias, "em relação a marcar com " + DIAS_REFERENCIA + " dias", atual,
                        dias == DIAS_REFERENCIA ? null : entrada(idade, DIAS_REFERENCIA, dia, lembrete, total, faltas)),
                fator("Lembrete", lembrete ? "Com lembrete" : "Sem lembrete",
                        lembrete ? "em relação a não receber lembrete" : "em relação a receber lembrete", atual,
                        entrada(idade, dias, dia, !lembrete, total, faltas)),
                fator("Histórico", textoHistorico, comparacaoHistorico, atual, semEsseHistorico),
                fator("Idade", idade + (idade == 1 ? " ano" : " anos"), "em relação a " + IDADE_REFERENCIA + " anos, a idade mediana da base", atual,
                        idade == IDADE_REFERENCIA ? null : entrada(IDADE_REFERENCIA, dias, dia, lembrete, total, faltas)),
                fator("Dia da semana", NOMES_DIAS[dia], "em relação a uma quarta-feira", atual,
                        dia == DIA_REFERENCIA ? null : entrada(idade, dias, DIA_REFERENCIA, lembrete, total, faltas))));
        fatores.sort(Comparator.comparingDouble((FatorRiscoResponse f) -> f.efeito() == null ? -1 : Math.abs(f.efeito())).reversed());
        return new ExplicacaoRiscoResponse(true, fatores);
    }

    private FatorRiscoResponse fator(String nome, String valor, String comparacao, double atual, IaRequest alternativa) {
        if (alternativa == null) {
            return new FatorRiscoResponse(nome, valor, 0.0, comparacao);
        }
        Double efeito = iaClient.preverFalta(alternativa)
                .map(ia -> Math.round((atual - ia.getProbabilidadeFalta()) * 1000) / 10.0)
                .orElse(null);
        return new FatorRiscoResponse(nome, valor, efeito, comparacao);
    }

    private static IaRequest entrada(int idade, long dias, int diaSemana, boolean lembrete, int total, int faltas) {
        IaRequest entrada = new IaRequest();
        entrada.setIdade(idade);
        entrada.setDiasAntecedencia(dias);
        entrada.setMesmoDia(dias == 0);
        entrada.setDiaSemana(diaSemana);
        entrada.setRecebeuLembrete(lembrete);
        entrada.setConsultasAnteriores(total);
        entrada.setFaltasAnteriores(faltas);
        entrada.setTaxaFaltasAnterior(total == 0 ? 0.0 : Math.round(faltas * 100.0 / total) / 100.0);
        entrada.setPrimeiraConsulta(total == 0);
        return entrada;
    }

    private static String motivo(IaResponse ia) {
        if (ia.getMotivos() == null || ia.getMotivos().isEmpty()) {
            return null;
        }
        String texto = String.join("; ", ia.getMotivos());
        return texto.length() > TAMANHO_MOTIVO ? texto.substring(0, TAMANHO_MOTIVO) : texto;
    }
}
