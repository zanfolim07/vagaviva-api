UPDATE tb_usuario SET ds_senha_hash = '$2a$10$gZmRQOdAFmP3NLRmOir6Ke2d1Ve/PagP3MAex8utHsH3YgrlnE7j.'
 WHERE ds_email IN ('marcos.andrade@saude.sp.gov.br', 'regina.costa@saude.sp.gov.br',
                    'paulo.ferreira@saude.sp.gov.br', 'sergio.lima@saude.sp.gov.br');

COMMIT;
