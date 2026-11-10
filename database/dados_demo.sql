UPDATE tb_consulta SET dt_agendamento = DATE '1999-12-31', dt_consulta = DATE '2000-01-01' + cd_consulta / 48
 WHERE cd_consulta BETWEEN 1 AND 8;

UPDATE tb_consulta
   SET cd_paciente = cd_consulta,
       dt_agendamento = TRUNC(CAST(SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo' AS DATE)) - DECODE(cd_consulta, 1, 40, 2, 14, 3, 5, 4, 7, 5, 18, 6, 21, 7, 33, 9),
       dt_consulta = TRUNC(CAST(SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo' AS DATE), 'HH') - 1/24 + (cd_consulta - 1) / 48,
       ds_status = DECODE(cd_consulta, 1, 'AGENDADA', 2, 'AGENDADA', 5, 'AGENDADA', 7, 'LIBERADA', 'CONFIRMADA')
 WHERE cd_consulta BETWEEN 1 AND 8;

UPDATE tb_consulta SET vl_risco_falta = 0.346, ds_nivel_risco = 'MEDIO', ds_motivo_risco = 'Agendada há 40 dias, Sem lembrete, Primeira consulta', fl_lembrete = 'N', ds_token_paciente = '016c85ee1b1672ec' WHERE cd_consulta = 1;
UPDATE tb_consulta SET vl_risco_falta = 0.361, ds_nivel_risco = 'ALTO', ds_motivo_risco = 'Faltou em 1 das 2 consultas anteriores', fl_lembrete = 'S', ds_token_paciente = '085daaa38ff71c3a' WHERE cd_consulta = 2;
UPDATE tb_consulta SET vl_risco_falta = 0.313, ds_nivel_risco = 'MEDIO', ds_motivo_risco = 'Primeira consulta', fl_lembrete = 'S', ds_token_paciente = '7fa525c6770a753e' WHERE cd_consulta = 3;
UPDATE tb_consulta SET vl_risco_falta = 0.203, ds_nivel_risco = 'MEDIO', ds_motivo_risco = 'Primeira consulta', fl_lembrete = 'S', ds_token_paciente = 'b75631c7143cf872' WHERE cd_consulta = 4;
UPDATE tb_consulta SET vl_risco_falta = 0.482, ds_nivel_risco = 'ALTO', ds_motivo_risco = 'Faltou em 2 das 2 consultas anteriores', fl_lembrete = 'S', ds_token_paciente = '27bc3f97dc7c1560' WHERE cd_consulta = 5;
UPDATE tb_consulta SET vl_risco_falta = 0.35, ds_nivel_risco = 'ALTO', ds_motivo_risco = 'Primeira consulta', fl_lembrete = 'S', ds_token_paciente = 'c157e428e3527359' WHERE cd_consulta = 6;
UPDATE tb_consulta SET vl_risco_falta = 0.342, ds_nivel_risco = 'MEDIO', ds_motivo_risco = 'Agendada há 33 dias, Sem lembrete, Primeira consulta', fl_lembrete = 'N', ds_token_paciente = 'cc6d198ecbdb5d3a' WHERE cd_consulta = 7;
UPDATE tb_consulta SET vl_risco_falta = 0.179, ds_nivel_risco = 'BAIXO', ds_motivo_risco = 'Primeira consulta', fl_lembrete = 'S', ds_token_paciente = '25620f2d76dcba2f' WHERE cd_consulta = 8;

DELETE FROM tb_oferta_vaga;

MERGE INTO tb_lista_espera le USING (SELECT 5 AS id FROM dual) d ON (le.cd_espera = d.id)
WHEN NOT MATCHED THEN INSERT (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status)
VALUES (5, 14, 1, 1, 3, NULL, NULL, DATE '2026-09-25', 'P4', 'Avaliação de rotina, condição crônica compensada', 'N', NULL, 'S', 'b447c1bf756a3abd', 5, 'AGUARDANDO');

UPDATE tb_lista_espera
   SET ds_status = DECODE(cd_espera, 1, 'OFERTADA', 'AGUARDANDO'),
       cd_consulta_ofertada = DECODE(cd_espera, 1, 7, NULL),
       dt_entrada = TRUNC(CAST(SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo' AS DATE)) - DECODE(cd_espera, 1, 16, 2, 122, 3, 48, 4, 25, 5, 55, 6, 30, 7, 39, 22)
 WHERE cd_espera BETWEEN 1 AND 8;

INSERT INTO tb_oferta_vaga (cd_espera, cd_consulta, dt_limite)
SELECT 1, 7, dt_consulta - 1/48 FROM tb_consulta WHERE cd_consulta = 7;

COMMIT;
