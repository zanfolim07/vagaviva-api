-- Permite novo pedido após conclusão ou desistência, preservando o histórico.
-- Pode ser executado novamente com segurança.
DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM user_constraints
   WHERE table_name = 'TB_LISTA_ESPERA'
     AND constraint_name = 'UK_ESP_PACIENTE_ESP';
  IF v_count > 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE tb_lista_espera DROP CONSTRAINT uk_esp_paciente_esp';
  END IF;

  SELECT COUNT(*) INTO v_count FROM user_indexes
   WHERE table_name = 'TB_LISTA_ESPERA'
     AND index_name = 'UK_ESP_PACIENTE_ESP';
  IF v_count > 0 THEN
    EXECUTE IMMEDIATE 'DROP INDEX uk_esp_paciente_esp';
  END IF;

  SELECT COUNT(*) INTO v_count FROM user_indexes
   WHERE table_name = 'TB_LISTA_ESPERA'
     AND index_name = 'UK_ESP_PACIENTE_ESP_ATIVOS';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'~CREATE UNIQUE INDEX uk_esp_paciente_esp_ativos ON tb_lista_espera (
      CASE WHEN ds_status IN ('AGUARDANDO', 'OFERTADA') THEN cd_paciente END,
      CASE WHEN ds_status IN ('AGUARDANDO', 'OFERTADA') THEN cd_especialidade END
    )~';
  END IF;
END;
/

COMMIT;
