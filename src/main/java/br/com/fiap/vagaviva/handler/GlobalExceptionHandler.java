package br.com.fiap.vagaviva.handler;

import br.com.fiap.vagaviva.exception.AcessoNegadoException;
import br.com.fiap.vagaviva.exception.HorarioOcupadoException;
import br.com.fiap.vagaviva.exception.MuitasTentativasException;
import br.com.fiap.vagaviva.exception.NaoAutenticadoException;
import br.com.fiap.vagaviva.exception.RecursoNaoEncontradoException;
import br.com.fiap.vagaviva.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Pattern CONSTRAINT = Pattern.compile("\\(([A-Z0-9_$#]+)\\.([A-Z0-9_$#]+)\\)");

    private static final Map<String, String> MENSAGENS = Map.ofEntries(
            Map.entry("UK_UNIDADE_NOME", "Já existe uma unidade com esse nome."),
            Map.entry("UK_ESP_NOME", "Já existe uma especialidade com esse nome."),
            Map.entry("UK_PROF_REGISTRO", "Já existe um profissional com esse registro no conselho."),
            Map.entry("PK_PROF_ESP", "A mesma especialidade foi informada mais de uma vez para o profissional."),
            Map.entry("UK_PAC_CNS", "Já existe um paciente com esse Cartão SUS."),
            Map.entry("UK_CONS_HORARIO", "O profissional já tem consulta nesse dia e horário (RN01)."),
            Map.entry("UK_ESP_PACIENTE_ESP", "O paciente já tem um pedido na lista de espera dessa especialidade (RN09)."),
            Map.entry("FK_CONS_PACIENTE", "O paciente tem consultas registradas e não pode ser removido (RN08)."),
            Map.entry("FK_ESP_PACIENTE", "O paciente tem pedidos na lista de espera e não pode ser removido."),
            Map.entry("FK_PROF_UNIDADE", "A unidade tem profissionais vinculados e não pode ser removida."),
            Map.entry("FK_ESP_UNIDADE", "A unidade tem pedidos na lista de espera e não pode ser removida."),
            Map.entry("UK_USU_EMAIL", "Já existe uma pessoa da equipe com esse e-mail."),
            Map.entry("UK_USU_CONVITE", "Esse código de convite já está em uso. Tente novamente."),
            Map.entry("FK_USU_UNIDADE", "A unidade tem usuários da equipe vinculados e não pode ser removida."),
            Map.entry("FK_CONS_PROF", "O profissional tem consultas registradas e não pode ser removido."),
            Map.entry("FK_CONS_ESPECIALIDADE", "A especialidade tem consultas registradas e não pode ser removida."),
            Map.entry("FK_ESP_SOLICITANTE", "O profissional fez pedidos para a lista de espera e não pode ser removido."),
            Map.entry("FK_ESP_PREF", "O profissional é o preferido de pedidos da lista de espera e não pode ser removido."),
            Map.entry("FK_PROFESP_ESP", "A especialidade tem profissionais vinculados e não pode ser removida."),
            Map.entry("FK_ESP_ESPECIALIDADE", "A especialidade tem pedidos na lista de espera e não pode ser removida."),
            Map.entry("FK_ESP_CONSULTA", "A consulta está oferecida a um pedido da lista de espera e não pode ser removida."),
            Map.entry("CK_ESP_PRAZO_P1", "Só pedidos de classe P1 podem ter prazo legal."),
            Map.entry("CK_CONS_DATAS", "A data da consulta precisa ser posterior à data do agendamento.")
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroResponse.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErroResponse.CampoErro(f.getField(), f.getDefaultMessage()))
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos não foram preenchidos corretamente.", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON inválido",
                "O corpo da requisição está vazio, malformado ou tem um valor no formato errado "
                        + "(datas no formato 2026-11-19 e data e hora no formato 2026-11-19T08:30:00).", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O valor informado em '" + ex.getName() + "' não é válido.", req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório",
                "Informe o parâmetro '" + ex.getParameterName() + "'.", req, null);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Rota não encontrada",
                "A rota " + req.getRequestURI() + " não existe na API.", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado",
                "O método " + ex.getMethod() + " não é aceito nesta rota.", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tipoNaoSuportado(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato não suportado",
                "Envie o corpo em JSON, com o header Content-Type: application/json.", req, null);
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErroResponse> naoAutenticado(NaoAutenticadoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, "Não autenticado", ex.getMessage(), req, null);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AcessoNegadoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.FORBIDDEN, "Sem permissão", ex.getMessage(), req, null);
    }

    @ExceptionHandler(MuitasTentativasException.class)
    public ResponseEntity<ErroResponse> muitasTentativas(MuitasTentativasException ex, HttpServletRequest req) {
        return resposta(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas", ex.getMessage(), req, null);
    }

    @ExceptionHandler(HorarioOcupadoException.class)
    public ResponseEntity<ErroResponse> horarioOcupado(HorarioOcupadoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Horário ocupado", ex.getMessage(), req, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Regra de negócio", ex.getMessage(), req, null);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErroResponse> banco(SQLException ex, HttpServletRequest req) {
        int codigo = ex.getErrorCode();
        String constraint = nomeConstraint(ex.getMessage());
        switch (codigo) {
            case 1 -> {
                return resposta(HttpStatus.CONFLICT, "Registro duplicado",
                        MENSAGENS.getOrDefault(constraint, "Já existe um registro com esses dados."), req, null);
            }
            case 2292 -> {
                return resposta(HttpStatus.CONFLICT, "Registro com dependentes",
                        MENSAGENS.getOrDefault(constraint, "O registro tem dados ligados a ele e não pode ser removido."), req, null);
            }
            case 2291 -> {
                return resposta(HttpStatus.CONFLICT, "Registro relacionado inexistente",
                        "Um dos registros relacionados informados não existe.", req, null);
            }
            case 2290 -> {
                return resposta(HttpStatus.CONFLICT, "Dados fora das regras",
                        MENSAGENS.getOrDefault(constraint, "Algum valor informado não é aceito pelas regras do banco."), req, null);
            }
            case 1400, 12899, 1438 -> {
                return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos",
                        "Algum valor informado está vazio ou maior que o permitido.", req, null);
            }
            default -> {
                log.error("Erro de banco não tratado em {} (ORA-{})", req.getRequestURI(), codigo, ex);
                return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                        "Não foi possível acessar o banco de dados. Tente novamente em instantes.", req, null);
            }
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em instantes.", req, null);
    }

    private static String nomeConstraint(String mensagem) {
        if (mensagem == null) {
            return "";
        }
        Matcher m = CONSTRAINT.matcher(mensagem);
        return m.find() ? m.group(2) : "";
    }

    private static ResponseEntity<ErroResponse> resposta(HttpStatus status, String erro, String mensagem,
                                                         HttpServletRequest req, List<ErroResponse.CampoErro> campos) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), erro, mensagem, req.getRequestURI(), campos));
    }
}
