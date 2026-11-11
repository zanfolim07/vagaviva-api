BEGIN
  FOR t IN (SELECT table_name FROM user_tables
             WHERE table_name IN ('TB_OFERTA_VAGA', 'TB_LISTA_ESPERA', 'TB_CONSULTA', 'TB_USUARIO', 'TB_PACIENTE', 'TB_PROFISSIONAL_ESPECIALIDADE', 'TB_PROFISSIONAL', 'TB_ESPECIALIDADE', 'TB_UNIDADE')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
  END LOOP;
  FOR s IN (SELECT sequence_name FROM user_sequences
             WHERE sequence_name IN ('SQ_TB_UNIDADE', 'SQ_TB_ESPECIALIDADE', 'SQ_TB_PROFISSIONAL', 'SQ_TB_PACIENTE', 'SQ_TB_CONSULTA', 'SQ_TB_LISTA_ESPERA', 'SQ_TB_USUARIO')) LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
  END LOOP;
END;
/

CREATE TABLE tb_unidade (
    cd_unidade    NUMBER(10)    CONSTRAINT pk_unidade PRIMARY KEY,
    nm_unidade    VARCHAR2(100) CONSTRAINT nn_unidade_nome NOT NULL,
    ds_endereco   VARCHAR2(150) CONSTRAINT nn_unidade_end NOT NULL,
    nm_bairro     VARCHAR2(60)  CONSTRAINT nn_unidade_bairro NOT NULL,
    nm_cidade     VARCHAR2(60)  CONSTRAINT nn_unidade_cidade NOT NULL,
    nr_telefone   VARCHAR2(15),
    CONSTRAINT uk_unidade_nome UNIQUE (nm_unidade)
);
COMMENT ON TABLE tb_unidade IS 'Unidades Básicas de Saúde (UBS) atendidas pela Vaga Viva.';

CREATE TABLE tb_especialidade (
    cd_especialidade  NUMBER(10)   CONSTRAINT pk_especialidade PRIMARY KEY,
    nm_especialidade  VARCHAR2(60) CONSTRAINT nn_esp_nome NOT NULL,
    CONSTRAINT uk_esp_nome UNIQUE (nm_especialidade)
);
COMMENT ON TABLE tb_especialidade IS 'Especialidades atendidas nas unidades, como clínica geral e pediatria.';

CREATE TABLE tb_profissional (
    cd_profissional  NUMBER(10)    CONSTRAINT pk_profissional PRIMARY KEY,
    cd_unidade       NUMBER(10)    CONSTRAINT nn_prof_unidade NOT NULL,
    nm_profissional  VARCHAR2(100) CONSTRAINT nn_prof_nome NOT NULL,
    nr_registro      VARCHAR2(20)  CONSTRAINT nn_prof_registro NOT NULL,
    ds_categoria     VARCHAR2(10)  CONSTRAINT nn_prof_categoria NOT NULL,
    CONSTRAINT uk_prof_registro UNIQUE (nr_registro),
    CONSTRAINT ck_prof_categoria CHECK (ds_categoria IN ('MEDICO', 'ENFERMEIRO')),
    CONSTRAINT fk_prof_unidade FOREIGN KEY (cd_unidade) REFERENCES tb_unidade (cd_unidade)
);
COMMENT ON TABLE tb_profissional IS 'Médicos e enfermeiros que atendem nas unidades e fazem pedidos para a lista de espera.';

CREATE TABLE tb_profissional_especialidade (
    cd_profissional   NUMBER(10),
    cd_especialidade  NUMBER(10),
    CONSTRAINT pk_prof_esp PRIMARY KEY (cd_profissional, cd_especialidade),
    CONSTRAINT fk_profesp_prof FOREIGN KEY (cd_profissional) REFERENCES tb_profissional (cd_profissional),
    CONSTRAINT fk_profesp_esp FOREIGN KEY (cd_especialidade) REFERENCES tb_especialidade (cd_especialidade)
);
COMMENT ON TABLE tb_profissional_especialidade IS 'Tabela associativa do N:N: um profissional atende várias especialidades e uma especialidade tem vários profissionais.';

CREATE TABLE tb_paciente (
    cd_paciente           NUMBER(10)    CONSTRAINT pk_paciente PRIMARY KEY,
    nm_paciente           VARCHAR2(100) CONSTRAINT nn_pac_nome NOT NULL,
    nr_cns                CHAR(15)      CONSTRAINT nn_pac_cns NOT NULL,
    dt_nascimento         DATE          CONSTRAINT nn_pac_nasc NOT NULL,
    nr_telefone           VARCHAR2(15)  CONSTRAINT nn_pac_tel NOT NULL,
    nm_bairro             VARCHAR2(60),
    ds_canal_contato      VARCHAR2(10)  DEFAULT 'WHATSAPP' CONSTRAINT nn_pac_canal NOT NULL,
    nr_telefone_familiar  VARCHAR2(15),
    CONSTRAINT uk_pac_cns UNIQUE (nr_cns),
    CONSTRAINT ck_pac_canal CHECK (ds_canal_contato IN ('WHATSAPP', 'SMS', 'LIGACAO'))
);
COMMENT ON TABLE tb_paciente IS 'Pacientes do SUS atendidos pelas unidades. nr_cns é o número do Cartão Nacional de Saúde.';

CREATE TABLE tb_consulta (
    cd_consulta        NUMBER(10)    CONSTRAINT pk_consulta PRIMARY KEY,
    cd_paciente        NUMBER(10)    CONSTRAINT nn_cons_paciente NOT NULL,
    cd_profissional    NUMBER(10)    CONSTRAINT nn_cons_prof NOT NULL,
    cd_especialidade   NUMBER(10)    CONSTRAINT nn_cons_especialidade NOT NULL,
    dt_agendamento     DATE          DEFAULT SYSDATE CONSTRAINT nn_cons_dtag NOT NULL,
    dt_consulta        DATE          CONSTRAINT nn_cons_dtcons NOT NULL,
    ds_status          VARCHAR2(12)  DEFAULT 'AGENDADA' CONSTRAINT nn_cons_status NOT NULL,
    vl_risco_falta     NUMBER(4,3),
    ds_nivel_risco     VARCHAR2(13)  DEFAULT 'NAO_CALCULADO' CONSTRAINT nn_cons_nivel NOT NULL,
    ds_motivo_risco    VARCHAR2(200),
    fl_lembrete        CHAR(1)       DEFAULT 'S' CONSTRAINT nn_cons_lembrete NOT NULL,
    ds_token_paciente  VARCHAR2(32)  CONSTRAINT nn_cons_token NOT NULL,
    CONSTRAINT uk_cons_token UNIQUE (ds_token_paciente),
    CONSTRAINT ck_cons_status CHECK (ds_status IN ('AGENDADA', 'CONFIRMADA', 'REALIZADA', 'FALTA', 'CANCELADA', 'LIBERADA')),
    CONSTRAINT ck_cons_risco CHECK (vl_risco_falta BETWEEN 0 AND 1),
    CONSTRAINT ck_cons_nivel CHECK (ds_nivel_risco IN ('BAIXO', 'MEDIO', 'ALTO', 'NAO_CALCULADO')),
    CONSTRAINT ck_cons_lembrete CHECK (fl_lembrete IN ('S', 'N')),
    CONSTRAINT ck_cons_datas CHECK (dt_consulta > dt_agendamento),
    CONSTRAINT fk_cons_paciente FOREIGN KEY (cd_paciente) REFERENCES tb_paciente (cd_paciente),
    CONSTRAINT fk_cons_prof FOREIGN KEY (cd_profissional) REFERENCES tb_profissional (cd_profissional),
    CONSTRAINT fk_cons_especialidade FOREIGN KEY (cd_especialidade) REFERENCES tb_especialidade (cd_especialidade)
);
COMMENT ON TABLE tb_consulta IS 'Consultas agendadas, com o risco de falta calculado pela IA. Um profissional não pode ter duas consultas no mesmo horário (RN01).';

CREATE UNIQUE INDEX uk_cons_horario ON tb_consulta (
    CASE WHEN ds_status NOT IN ('CANCELADA', 'LIBERADA') THEN cd_profissional END,
    CASE WHEN ds_status NOT IN ('CANCELADA', 'LIBERADA') THEN dt_consulta END
);

CREATE TABLE tb_lista_espera (
    cd_espera                    NUMBER(10)    CONSTRAINT pk_lista_espera PRIMARY KEY,
    cd_paciente                  NUMBER(10)    CONSTRAINT nn_esp_paciente NOT NULL,
    cd_especialidade             NUMBER(10)    CONSTRAINT nn_esp_especialidade NOT NULL,
    cd_unidade                   NUMBER(10)    CONSTRAINT nn_esp_unidade NOT NULL,
    cd_profissional_solicitante  NUMBER(10)    CONSTRAINT nn_esp_solicitante NOT NULL,
    cd_profissional_pref         NUMBER(10),
    cd_consulta_ofertada         NUMBER(10),
    dt_entrada                   DATE          DEFAULT SYSDATE CONSTRAINT nn_esp_entrada NOT NULL,
    ds_classe_risco              CHAR(2)       CONSTRAINT nn_esp_classe NOT NULL,
    ds_justificativa_clinica     VARCHAR2(500) CONSTRAINT nn_esp_justificativa NOT NULL,
    fl_prazo_legal               CHAR(1)       DEFAULT 'N' CONSTRAINT nn_esp_prazo NOT NULL,
    ds_prioridade_legal          VARCHAR2(8),
    fl_aceita_mesmo_dia          CHAR(1)       DEFAULT 'N' CONSTRAINT nn_esp_mesmodia NOT NULL,
    ds_token_paciente            VARCHAR2(32)  CONSTRAINT nn_esp_token NOT NULL,
    nr_ultima_posicao            NUMBER(4),
    ds_status                    VARCHAR2(10)  DEFAULT 'AGUARDANDO' CONSTRAINT nn_esp_status NOT NULL,
    CONSTRAINT uk_esp_token UNIQUE (ds_token_paciente),
    CONSTRAINT ck_esp_classe CHECK (ds_classe_risco IN ('P1', 'P2', 'P3', 'P4')),
    CONSTRAINT ck_esp_prazo CHECK (fl_prazo_legal IN ('S', 'N')),
    CONSTRAINT ck_esp_prazo_p1 CHECK (fl_prazo_legal = 'N' OR ds_classe_risco = 'P1'),
    CONSTRAINT ck_esp_prioridade CHECK (ds_prioridade_legal IN ('60+', '80+', 'GESTANTE', 'PCD')),
    CONSTRAINT ck_esp_mesmodia CHECK (fl_aceita_mesmo_dia IN ('S', 'N')),
    CONSTRAINT ck_esp_status CHECK (ds_status IN ('AGUARDANDO', 'OFERTADA', 'ATENDIDA', 'DESISTIU')),
    CONSTRAINT ck_esp_justificativa CHECK (LENGTH(ds_justificativa_clinica) >= 20),
    CONSTRAINT fk_esp_paciente FOREIGN KEY (cd_paciente) REFERENCES tb_paciente (cd_paciente),
    CONSTRAINT fk_esp_especialidade FOREIGN KEY (cd_especialidade) REFERENCES tb_especialidade (cd_especialidade),
    CONSTRAINT fk_esp_unidade FOREIGN KEY (cd_unidade) REFERENCES tb_unidade (cd_unidade),
    CONSTRAINT fk_esp_solicitante FOREIGN KEY (cd_profissional_solicitante) REFERENCES tb_profissional (cd_profissional),
    CONSTRAINT fk_esp_pref FOREIGN KEY (cd_profissional_pref) REFERENCES tb_profissional (cd_profissional),
    CONSTRAINT fk_esp_consulta FOREIGN KEY (cd_consulta_ofertada) REFERENCES tb_consulta (cd_consulta)
);
CREATE UNIQUE INDEX uk_esp_paciente_esp_ativos ON tb_lista_espera (
    CASE WHEN ds_status IN ('AGUARDANDO', 'OFERTADA') THEN cd_paciente END,
    CASE WHEN ds_status IN ('AGUARDANDO', 'OFERTADA') THEN cd_especialidade END
);

COMMENT ON TABLE tb_lista_espera IS 'Fila de espera por especialidade e unidade, ordenada pela classe de risco do pedido e pelo tempo de espera (RN12).';

CREATE TABLE tb_oferta_vaga (
    cd_espera     NUMBER(10),
    cd_consulta   NUMBER(10),
    dt_limite     DATE CONSTRAINT nn_oferta_limite NOT NULL,
    ds_resultado  VARCHAR2(10),
    CONSTRAINT pk_oferta_vaga PRIMARY KEY (cd_espera, cd_consulta),
    CONSTRAINT ck_oferta_resultado CHECK (ds_resultado IN ('RECUSADA', 'EXPIRADA')),
    CONSTRAINT fk_oferta_espera FOREIGN KEY (cd_espera) REFERENCES tb_lista_espera (cd_espera) ON DELETE CASCADE,
    CONSTRAINT fk_oferta_consulta FOREIGN KEY (cd_consulta) REFERENCES tb_consulta (cd_consulta) ON DELETE CASCADE
);
COMMENT ON TABLE tb_oferta_vaga IS 'Prazo de cada vaga oferecida e quem já recusou ou deixou o prazo passar.';

CREATE TABLE tb_usuario (
    cd_usuario            NUMBER(10)    CONSTRAINT pk_usuario PRIMARY KEY,
    cd_unidade            NUMBER(10)    CONSTRAINT nn_usu_unidade NOT NULL,
    nm_usuario            VARCHAR2(100) CONSTRAINT nn_usu_nome NOT NULL,
    ds_email              VARCHAR2(120) CONSTRAINT nn_usu_email NOT NULL,
    ds_senha_hash         VARCHAR2(100),
    ds_perfil             VARCHAR2(10)  CONSTRAINT nn_usu_perfil NOT NULL,
    ds_status             VARCHAR2(10)  DEFAULT 'PENDENTE' CONSTRAINT nn_usu_status NOT NULL,
    ds_codigo_convite     VARCHAR2(7),
    dt_expiracao_convite  DATE,
    dt_ultimo_acesso      DATE,
    CONSTRAINT uk_usu_email UNIQUE (ds_email),
    CONSTRAINT uk_usu_convite UNIQUE (ds_codigo_convite),
    CONSTRAINT ck_usu_perfil CHECK (ds_perfil IN ('RECEPCAO', 'GESTAO')),
    CONSTRAINT ck_usu_status CHECK (ds_status IN ('PENDENTE', 'ATIVO', 'DESATIVADO')),
    CONSTRAINT fk_usu_unidade FOREIGN KEY (cd_unidade) REFERENCES tb_unidade (cd_unidade)
);
COMMENT ON TABLE tb_usuario IS 'Equipe da unidade com acesso ao sistema (recepção e gestão). A conta é criada pelo gestor e ativada por convite.';

CREATE SEQUENCE sq_tb_unidade START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_especialidade START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_profissional START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_paciente START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_consulta START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_lista_espera START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_usuario START WITH 1 INCREMENT BY 1 NOCACHE;

INSERT INTO tb_unidade (cd_unidade, nm_unidade, ds_endereco, nm_bairro, nm_cidade, nr_telefone) VALUES (sq_tb_unidade.NEXTVAL, 'UBS Centro', 'Rua XV de Novembro, 120', 'Centro', 'São Paulo', '1131110001');
INSERT INTO tb_unidade (cd_unidade, nm_unidade, ds_endereco, nm_bairro, nm_cidade, nr_telefone) VALUES (sq_tb_unidade.NEXTVAL, 'UBS Jardim Norte', 'Av. das Palmeiras, 845', 'Jardim Norte', 'São Paulo', '1131110002');
INSERT INTO tb_unidade (cd_unidade, nm_unidade, ds_endereco, nm_bairro, nm_cidade, nr_telefone) VALUES (sq_tb_unidade.NEXTVAL, 'UBS Vila Nova', 'Rua dos Ipês, 33', 'Vila Nova', 'São Paulo', '1131110003');

INSERT INTO tb_especialidade (cd_especialidade, nm_especialidade) VALUES (sq_tb_especialidade.NEXTVAL, 'Clínica geral');
INSERT INTO tb_especialidade (cd_especialidade, nm_especialidade) VALUES (sq_tb_especialidade.NEXTVAL, 'Pediatria');
INSERT INTO tb_especialidade (cd_especialidade, nm_especialidade) VALUES (sq_tb_especialidade.NEXTVAL, 'Enfermagem');
INSERT INTO tb_especialidade (cd_especialidade, nm_especialidade) VALUES (sq_tb_especialidade.NEXTVAL, 'Ginecologia');

INSERT INTO tb_profissional (cd_profissional, cd_unidade, nm_profissional, nr_registro, ds_categoria) VALUES (sq_tb_profissional.NEXTVAL, 1, 'Helena Lima', 'CRM-SP 123456', 'MEDICO');
INSERT INTO tb_profissional (cd_profissional, cd_unidade, nm_profissional, nr_registro, ds_categoria) VALUES (sq_tb_profissional.NEXTVAL, 1, 'Ricardo Souza', 'CRM-SP 234567', 'MEDICO');
INSERT INTO tb_profissional (cd_profissional, cd_unidade, nm_profissional, nr_registro, ds_categoria) VALUES (sq_tb_profissional.NEXTVAL, 1, 'Paula Ribeiro', 'COREN-SP 345678', 'ENFERMEIRO');
INSERT INTO tb_profissional (cd_profissional, cd_unidade, nm_profissional, nr_registro, ds_categoria) VALUES (sq_tb_profissional.NEXTVAL, 2, 'Bruno Castro', 'CRM-SP 456789', 'MEDICO');
INSERT INTO tb_profissional (cd_profissional, cd_unidade, nm_profissional, nr_registro, ds_categoria) VALUES (sq_tb_profissional.NEXTVAL, 2, 'Carla Reis', 'CRM-SP 567890', 'MEDICO');
INSERT INTO tb_profissional (cd_profissional, cd_unidade, nm_profissional, nr_registro, ds_categoria) VALUES (sq_tb_profissional.NEXTVAL, 3, 'Tiago Teles', 'COREN-SP 678901', 'ENFERMEIRO');

INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (1, 1);
INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (2, 2);
INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (2, 1);
INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (3, 3);
INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (4, 1);
INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (4, 4);
INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (5, 2);
INSERT INTO tb_profissional_especialidade (cd_profissional, cd_especialidade) VALUES (6, 3);

INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Ana Souza', '700000000000001', DATE '1991-03-12', '11990000001', 'Centro', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Carlos Mendes', '700000000000002', DATE '1984-07-02', '11990000002', 'Centro', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Lúcia Ramos', '700000000000003', DATE '2019-05-20', '11990000003', 'Centro', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Pedro Alves', '700000000000004', DATE '1958-11-30', '11990000004', 'Bela Vista', 'SMS', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Marcos Vieira', '700000000000005', DATE '1979-01-15', '11990000005', 'Centro', 'LIGACAO', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Beatriz Nunes', '700000000000006', DATE '2021-09-08', '11990000006', 'Bela Vista', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Rafael Dias', '700000000000007', DATE '1996-04-22', '11990000007', 'Centro', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Sônia Prado', '700000000000008', DATE '1942-06-10', '11990000008', 'Centro', 'LIGACAO', '11990000018');
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Fernanda Lima', '700000000000009', DATE '2000-12-01', '11990000009', 'Jardim Norte', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Cláudia Nunes', '700000000000010', DATE '1970-02-18', '11990000010', 'Centro', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Jorge Lins', '700000000000011', DATE '1955-08-25', '11990000011', 'Centro', 'SMS', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'João Pereira', '700000000000012', DATE '1962-10-03', '11990000012', 'Centro', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Maria Lopes', '700000000000013', DATE '1988-03-27', '11990000013', 'Jardim Norte', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Rita Costa', '700000000000014', DATE '1993-12-14', '11990000014', 'Vila Nova', 'SMS', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Laura Mota', '700000000000015', DATE '2025-06-30', '11990000015', 'Centro', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Davi Rocha', '700000000000016', DATE '2018-01-09', '11990000016', 'Bela Vista', 'WHATSAPP', NULL);
INSERT INTO tb_paciente (cd_paciente, nm_paciente, nr_cns, dt_nascimento, nr_telefone, nm_bairro, ds_canal_contato, nr_telefone_familiar) VALUES (sq_tb_paciente.NEXTVAL, 'Célia Barros', '700000000000017', DATE '1940-04-05', '11990000017', 'Vila Nova', 'LIGACAO', '11990000027');

INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 1,  1, 1, DATE '2026-10-10', TO_DATE('2026-11-19 08:30', 'YYYY-MM-DD HH24:MI'), 'AGENDADA', 0.41, 'ALTO', 'Agendada há 40 dias, sem lembrete', 'N', '016c85ee1b1672ec');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 2,  1, 1, DATE '2026-11-05', TO_DATE('2026-11-19 09:00', 'YYYY-MM-DD HH24:MI'), 'AGENDADA', 0.27, 'MEDIO', 'Faltou em 1 das últimas 3 consultas', 'S', '085daaa38ff71c3a');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 3,  2, 2, DATE '2026-11-14', TO_DATE('2026-11-19 09:30', 'YYYY-MM-DD HH24:MI'), 'CONFIRMADA', 0.12, 'BAIXO', 'Agendada há 5 dias', 'S', '7fa525c6770a753e');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 4,  1, 1, DATE '2026-11-12', TO_DATE('2026-11-19 10:00', 'YYYY-MM-DD HH24:MI'), 'CONFIRMADA', 0.09, 'BAIXO', 'Lembrete recebido', 'S', 'b75631c7143cf872');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 5,  3, 3, DATE '2026-11-01', TO_DATE('2026-11-19 10:30', 'YYYY-MM-DD HH24:MI'), 'AGENDADA', 0.38, 'ALTO', 'Faltou nas 2 últimas consultas', 'S', '27bc3f97dc7c1560');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 6,  2, 2, DATE '2026-10-29', TO_DATE('2026-11-19 11:00', 'YYYY-MM-DD HH24:MI'), 'CONFIRMADA', 0.22, 'MEDIO', 'Sem histórico na unidade, agendada há 21 dias', 'S', 'c157e428e3527359');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 7,  1, 1, DATE '2026-10-17', TO_DATE('2026-11-19 14:00', 'YYYY-MM-DD HH24:MI'), 'LIBERADA', 0.36, 'ALTO', 'Agendada há 33 dias, sem lembrete', 'N', 'cc6d198ecbdb5d3a');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 8,  3, 3, DATE '2026-11-10', TO_DATE('2026-11-19 14:30', 'YYYY-MM-DD HH24:MI'), 'CONFIRMADA', 0.06, 'BAIXO', 'Lembrete recebido', 'S', '25620f2d76dcba2f');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 2,  1, 1, DATE '2026-08-20', TO_DATE('2026-09-10 09:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 0.18, 'BAIXO', 'Agendada há 21 dias', 'S', 'df4d8bcc2a959d73');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 2,  1, 1, DATE '2026-09-15', TO_DATE('2026-10-08 09:30', 'YYYY-MM-DD HH24:MI'), 'FALTA', 0.24, 'MEDIO', 'Agendada há 23 dias', 'S', 'a1f76126c2c8fe06');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 5,  3, 3, DATE '2026-08-25', TO_DATE('2026-09-22 10:00', 'YYYY-MM-DD HH24:MI'), 'FALTA', 0.29, 'MEDIO', 'Agendada há 28 dias', 'N', '80400e9f88e77a3e');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 5,  3, 3, DATE '2026-09-25', TO_DATE('2026-10-20 10:30', 'YYYY-MM-DD HH24:MI'), 'FALTA', 0.33, 'MEDIO', 'Faltou na última consulta', 'S', 'ff19e0347468a7d1');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 12,  1, 1, DATE '2026-08-30', TO_DATE('2026-09-24 08:30', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 0.15, 'BAIXO', 'Lembrete recebido', 'S', 'a60ad1fd444839e1');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 11,  1, 1, DATE '2026-09-01', TO_DATE('2026-10-01 11:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 0.21, 'MEDIO', 'Agendada há 30 dias', 'S', '6572e22c300feef9');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 13,  4, 4, DATE '2026-08-18', TO_DATE('2026-09-17 14:00', 'YYYY-MM-DD HH24:MI'), 'FALTA', 0.37, 'ALTO', 'Agendada há 30 dias, sem lembrete', 'N', 'e6bcc8530cc29874');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 13,  4, 4, DATE '2026-09-20', TO_DATE('2026-10-15 14:30', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 0.26, 'MEDIO', 'Faltou na última consulta', 'S', '8a985c5aed711766');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 10,  4, 4, DATE '2026-09-05', TO_DATE('2026-10-05 15:00', 'YYYY-MM-DD HH24:MI'), 'CANCELADA', 0.31, 'MEDIO', 'Agendada há 30 dias', 'S', '35a9868f1e0f19d8');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 14,  6, 3, DATE '2026-09-02', TO_DATE('2026-09-29 08:00', 'YYYY-MM-DD HH24:MI'), 'FALTA', 0.35, 'MEDIO', 'Agendada há 27 dias', 'N', '6fbc4ca5aa1873b3');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 14,  6, 3, DATE '2026-10-01', TO_DATE('2026-10-27 08:30', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 0.3, 'MEDIO', 'Faltou na última consulta', 'S', '49b438b587ad5425');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 17,  6, 3, DATE '2026-09-10', TO_DATE('2026-10-06 09:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 0.08, 'BAIXO', 'Lembrete recebido', 'S', '5fac25b6a01eccde');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 16,  5, 2, DATE '2026-09-12', TO_DATE('2026-10-09 10:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 0.14, 'BAIXO', 'Lembrete recebido', 'S', '8469c9ca9566d519');
INSERT INTO tb_consulta (cd_consulta, cd_paciente, cd_profissional, cd_especialidade, dt_agendamento, dt_consulta, ds_status, vl_risco_falta, ds_nivel_risco, ds_motivo_risco, fl_lembrete, ds_token_paciente) VALUES (sq_tb_consulta.NEXTVAL, 15,  2, 2, DATE '2026-09-28', TO_DATE('2026-10-14 09:00', 'YYYY-MM-DD HH24:MI'), 'FALTA', 0.25, 'MEDIO', 'Agendada há 16 dias', 'S', '71c9be6eb90fb752');

INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status) VALUES (sq_tb_lista_espera.NEXTVAL, 10, 1, 1, 1, NULL, 7, DATE '2026-11-03', 'P1', 'Nódulo palpável em mama, investigação de neoplasia maligna', 'S', NULL, 'S', '24da014ea6be8b7f', 1, 'OFERTADA');
INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status) VALUES (sq_tb_lista_espera.NEXTVAL, 11, 1, 1, 1, 1, NULL, DATE '2026-07-20', 'P3', 'Acompanhamento de hipertensão controlada, retorno de rotina', 'N', '60+', 'N', '79a125e1ae95b6aa', 2, 'AGUARDANDO');
INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status) VALUES (sq_tb_lista_espera.NEXTVAL, 12, 1, 1, 3, NULL, NULL, DATE '2026-10-02', 'P2', 'Retorno com exames laboratoriais alterados para avaliação', 'N', '60+', 'S', '6d1e892a92fce8b8', 5, 'AGUARDANDO');
INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status) VALUES (sq_tb_lista_espera.NEXTVAL, 13, 1, 1, 1, NULL, NULL, DATE '2026-10-25', 'P2', 'Dor lombar persistente há 2 meses, sem sinais de alerta', 'N', NULL, 'N', 'a5de6c50f09e9fd5', 4, 'AGUARDANDO');
INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status) VALUES (sq_tb_lista_espera.NEXTVAL, 14, 1, 1, 3, NULL, NULL, DATE '2026-09-25', 'P4', 'Avaliação de rotina, condição crônica compensada', 'N', NULL, 'S', 'b447c1bf756a3abd', 5, 'AGUARDANDO');
INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status) VALUES (sq_tb_lista_espera.NEXTVAL, 15, 2, 1, 2, NULL, NULL, DATE '2026-10-20', 'P2', 'Bebê com baixo ganho de peso, reavaliação pediátrica', 'N', NULL, 'S', '8f529208d17949ea', 1, 'AGUARDANDO');
INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status) VALUES (sq_tb_lista_espera.NEXTVAL, 16, 2, 1, 2, 2, NULL, DATE '2026-10-11', 'P3', 'Retorno de rotina após tratamento de asma', 'N', NULL, 'N', 'ffc9bc60d7cfd8c1', 2, 'AGUARDANDO');
INSERT INTO tb_lista_espera (cd_espera, cd_paciente, cd_especialidade, cd_unidade, cd_profissional_solicitante, cd_profissional_pref, cd_consulta_ofertada, dt_entrada, ds_classe_risco, ds_justificativa_clinica, fl_prazo_legal, ds_prioridade_legal, fl_aceita_mesmo_dia, ds_token_paciente, nr_ultima_posicao, ds_status) VALUES (sq_tb_lista_espera.NEXTVAL, 17, 3, 1, 3, NULL, NULL, DATE '2026-10-28', 'P4', 'Troca de curativo em lesão de pele já em cicatrização', 'N', '80+', 'S', '74729186902d38ee', 1, 'AGUARDANDO');

INSERT INTO tb_usuario (cd_usuario, cd_unidade, nm_usuario, ds_email, ds_senha_hash, ds_perfil, ds_status, ds_codigo_convite, dt_expiracao_convite, dt_ultimo_acesso) VALUES (sq_tb_usuario.NEXTVAL, 1, 'Marcos Andrade', 'marcos.andrade@saude.sp.gov.br', '$2a$10$exemploHashBcrypt01', 'GESTAO', 'ATIVO', NULL, NULL, DATE '2026-11-18');
INSERT INTO tb_usuario (cd_usuario, cd_unidade, nm_usuario, ds_email, ds_senha_hash, ds_perfil, ds_status, ds_codigo_convite, dt_expiracao_convite, dt_ultimo_acesso) VALUES (sq_tb_usuario.NEXTVAL, 1, 'Regina Costa', 'regina.costa@saude.sp.gov.br', '$2a$10$exemploHashBcrypt02', 'RECEPCAO', 'ATIVO', NULL, NULL, DATE '2026-11-19');
INSERT INTO tb_usuario (cd_usuario, cd_unidade, nm_usuario, ds_email, ds_senha_hash, ds_perfil, ds_status, ds_codigo_convite, dt_expiracao_convite, dt_ultimo_acesso) VALUES (sq_tb_usuario.NEXTVAL, 1, 'Paulo Ferreira', 'paulo.ferreira@saude.sp.gov.br', '$2a$10$exemploHashBcrypt03', 'RECEPCAO', 'ATIVO', NULL, NULL, DATE '2026-11-17');
INSERT INTO tb_usuario (cd_usuario, cd_unidade, nm_usuario, ds_email, ds_senha_hash, ds_perfil, ds_status, ds_codigo_convite, dt_expiracao_convite, dt_ultimo_acesso) VALUES (sq_tb_usuario.NEXTVAL, 1, 'Juliana Prado', 'juliana.prado@saude.sp.gov.br', NULL, 'RECEPCAO', 'PENDENTE', 'VV-4821', DATE '2026-11-25', NULL);
INSERT INTO tb_usuario (cd_usuario, cd_unidade, nm_usuario, ds_email, ds_senha_hash, ds_perfil, ds_status, ds_codigo_convite, dt_expiracao_convite, dt_ultimo_acesso) VALUES (sq_tb_usuario.NEXTVAL, 1, 'Sérgio Lima', 'sergio.lima@saude.sp.gov.br', '$2a$10$exemploHashBcrypt05', 'RECEPCAO', 'DESATIVADO', NULL, NULL, DATE '2026-10-30');

INSERT INTO tb_oferta_vaga (cd_espera, cd_consulta, dt_limite) VALUES (1, 7, TO_DATE('2026-11-18 18:00', 'YYYY-MM-DD HH24:MI'));

COMMIT;

SELECT c.cd_consulta,
       TO_CHAR(c.dt_consulta, 'HH24:MI') AS horario,
       p.nm_paciente,
       pr.nm_profissional,
       c.vl_risco_falta,
       c.ds_motivo_risco
  FROM tb_consulta c
 INNER JOIN tb_paciente p      ON p.cd_paciente = c.cd_paciente
 INNER JOIN tb_profissional pr ON pr.cd_profissional = c.cd_profissional
 WHERE TRUNC(c.dt_consulta) = DATE '2026-11-19'
   AND c.ds_nivel_risco = 'ALTO'
   AND c.ds_status = 'AGENDADA'
 ORDER BY c.vl_risco_falta DESC;

SELECT c.cd_consulta,
       TO_CHAR(c.dt_consulta, 'HH24:MI') AS horario,
       TRUNC(c.dt_consulta) - TRUNC(c.dt_agendamento) AS dias_antecedencia,
       ROUND(c.vl_risco_falta * 100) || '%' AS risco_percentual,
       c.ds_nivel_risco
  FROM tb_consulta c
 WHERE TRUNC(c.dt_consulta) = DATE '2026-11-19'
 ORDER BY dias_antecedencia DESC;

SELECT ds_status,
       COUNT(*) AS total_consultas
  FROM tb_consulta
 GROUP BY ds_status
 ORDER BY total_consultas DESC;

SELECT u.nm_unidade,
       COUNT(*) AS consultas_encerradas,
       SUM(CASE WHEN c.ds_status = 'FALTA' THEN 1 ELSE 0 END) AS faltas,
       ROUND(SUM(CASE WHEN c.ds_status = 'FALTA' THEN 1 ELSE 0 END) * 100 / COUNT(*), 1) AS taxa_falta_pct
  FROM tb_consulta c
 INNER JOIN tb_profissional pr ON pr.cd_profissional = c.cd_profissional
 INNER JOIN tb_unidade u       ON u.cd_unidade = pr.cd_unidade
 WHERE c.ds_status IN ('REALIZADA', 'FALTA')
 GROUP BY u.nm_unidade
HAVING SUM(CASE WHEN c.ds_status = 'FALTA' THEN 1 ELSE 0 END) * 100 / COUNT(*) > 35
 ORDER BY taxa_falta_pct DESC;

SELECT pr.nm_profissional,
       COUNT(c.cd_consulta)              AS consultas,
       ROUND(AVG(c.vl_risco_falta), 3)   AS risco_medio,
       MAX(c.vl_risco_falta)             AS risco_maximo,
       MIN(c.vl_risco_falta)             AS risco_minimo
  FROM tb_profissional pr
 INNER JOIN tb_consulta c ON c.cd_profissional = pr.cd_profissional
 GROUP BY pr.nm_profissional
 ORDER BY risco_medio DESC;

SELECT p.nm_paciente,
       e.ds_classe_risco,
       e.fl_prazo_legal,
       DATE '2026-11-19' - e.dt_entrada AS dias_espera,
       CASE e.ds_classe_risco WHEN 'P1' THEN 30 WHEN 'P2' THEN 90 WHEN 'P3' THEN 120 ELSE 180 END AS prazo_dias,
       CASE e.ds_classe_risco WHEN 'P1' THEN 100 WHEN 'P2' THEN 50 WHEN 'P3' THEN 20 ELSE 0 END
       + ROUND((DATE '2026-11-19' - e.dt_entrada) * 100 /
               CASE e.ds_classe_risco WHEN 'P1' THEN 30 WHEN 'P2' THEN 90 WHEN 'P3' THEN 120 ELSE 180 END) AS pontuacao,
       e.ds_status
  FROM tb_lista_espera e
 INNER JOIN tb_paciente p ON p.cd_paciente = e.cd_paciente
 WHERE e.cd_especialidade = 1
   AND e.cd_unidade = 1
   AND e.ds_status IN ('AGUARDANDO', 'OFERTADA')
 ORDER BY e.fl_prazo_legal DESC, pontuacao DESC, e.dt_entrada;

SELECT p.nm_paciente,
       COUNT(*) AS total_faltas
  FROM tb_consulta c
 INNER JOIN tb_paciente p ON p.cd_paciente = c.cd_paciente
 WHERE c.ds_status = 'FALTA'
 GROUP BY p.nm_paciente
HAVING COUNT(*) > (SELECT AVG(qtd)
                     FROM (SELECT COUNT(*) AS qtd
                             FROM tb_consulta
                            WHERE ds_status = 'FALTA'
                            GROUP BY cd_paciente))
 ORDER BY total_faltas DESC;

SELECT p.nm_paciente,
       COUNT(c.cd_consulta) AS total_consultas
  FROM tb_paciente p
  LEFT JOIN tb_consulta c ON c.cd_paciente = p.cd_paciente
 GROUP BY p.nm_paciente
 ORDER BY total_consultas, p.nm_paciente;

SELECT es.nm_especialidade,
       COUNT(e.cd_espera) AS pessoas_na_fila
  FROM tb_lista_espera e
 RIGHT JOIN tb_especialidade es ON es.cd_especialidade = e.cd_especialidade
 GROUP BY es.nm_especialidade
 ORDER BY pessoas_na_fila DESC;

SELECT u.nm_unidade,
       COUNT(*)            AS vagas_devolvidas,
       SUM(13.85)          AS valor_preservado_reais
  FROM tb_consulta c
 INNER JOIN tb_profissional pr ON pr.cd_profissional = c.cd_profissional
 INNER JOIN tb_unidade u       ON u.cd_unidade = pr.cd_unidade
 WHERE c.ds_status IN ('LIBERADA', 'CANCELADA')
 GROUP BY u.nm_unidade
 ORDER BY valor_preservado_reais DESC;

UPDATE tb_consulta
   SET ds_status = 'CANCELADA'
 WHERE cd_consulta = 1;

UPDATE tb_lista_espera
   SET ds_status = 'OFERTADA',
       cd_consulta_ofertada = 1
 WHERE cd_espera = 3;

DELETE FROM tb_lista_espera
 WHERE cd_espera = 5;

COMMIT;
