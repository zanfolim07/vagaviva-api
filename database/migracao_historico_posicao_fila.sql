DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM user_tables
   WHERE table_name = 'TB_HISTORICO_POSICAO_FILA';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'~CREATE TABLE tb_historico_posicao_fila (
      cd_espera       NUMBER(10)    CONSTRAINT nn_hist_pos_espera NOT NULL,
      dt_referencia   DATE          CONSTRAINT nn_hist_pos_data NOT NULL,
      nr_posicao      NUMBER(4)     CONSTRAINT nn_hist_pos_numero NOT NULL,
      CONSTRAINT pk_hist_pos_fila PRIMARY KEY (cd_espera, dt_referencia),
      CONSTRAINT fk_hist_pos_espera FOREIGN KEY (cd_espera)
        REFERENCES tb_lista_espera (cd_espera) ON DELETE CASCADE
    )~';
  END IF;
END;
/

COMMENT ON TABLE tb_historico_posicao_fila IS
  'Retratos diários da posição dos pedidos para calcular a variação semanal sem depender da frequência de consulta da tela.';

COMMIT;
