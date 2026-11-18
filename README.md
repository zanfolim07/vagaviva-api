# Vaga Viva · API Java

API REST da **Vaga Viva**, solução CitizenTech desenvolvida para a Global Solution 2026/2 da FIAP.

O sistema ajuda unidades básicas de saúde a organizar consultas, acompanhar o risco de falta e oferecer horários devolvidos a pacientes compatíveis da lista de espera. Esta API aplica as regras de negócio, persiste os dados no Oracle e integra-se ao front-end React e ao serviço de IA em Python/Flask.

## Integrantes · 1TDSPV

| Nome | RM |
| --- | --- |
| Fabio Cezare Almeida | RM572642 |
| Guilherme Zanfolim Nunes Farias | RM570983 |
| Jaime Ringel | RM562044 |
| Lucas Monteiro Dias da Costa | RM571388 |
| Mateus Magalhães Romão de Moraes | RM573371 |

## Links

- **API no Render:** <https://vagaviva-api.onrender.com>
- **API de IA (Python/Flask):** <https://vagaviva-ia.onrender.com>
- **Vídeo não listado:** <https://youtu.be/8T_0reS97Mg>

No plano gratuito do Render, serviços podem ficar inativos após um período sem uso. A primeira requisição pode demorar para acordá-los.

## Tecnologias

- Java 21 e Spring Boot 3.3.5
- JDBC com `PreparedStatement` e `DataSource` (sem JPA, Hibernate ou Spring Data)
- Oracle Database
- Bean Validation e ModelMapper
- JWT para autenticação, com senhas protegidas por BCrypt
- Maven, Docker e Render
- Serviço de previsão de risco em Python/Flask

## Organização do código

```text
src/main/java/br/com/fiap/vagaviva
├── model        Entidades do domínio e estados
├── dao          Consultas e gravações no Oracle via JDBC
├── dto          Formatos de entrada e saída da API
├── service      Regras de negócio e transações
├── controller   Rotas REST
├── client       Comunicação com a API de IA
├── config       Autenticação, CORS e configurações
├── exception    Exceções do domínio
└── handler      Tratamento padronizado de erros

database/        Scripts de criação, demonstração e migração do Oracle
```

## Como executar localmente

### 1. Requisitos

- JDK 21
- Maven
- Acesso ao Oracle da FIAP ou a um banco Oracle compatível

### 2. Preparar o banco

No SQL Developer, execute `database/VagaViva_BancoDeDados.sql`. **Atenção:** esse script apaga e recria as tabelas e sequences da aplicação, incluindo os dados existentes nelas. Use-o somente quando puder reiniciar o banco de demonstração.

Depois, para preparar os dados de demonstração, execute nesta ordem:

1. `database/senhas_demo.sql`
2. `database/dados_demo.sql`

O script principal já cria as tabelas de histórico de posição e de ofertas. Para um banco antigo, confira se ele possui essas estruturas e aplique, quando necessário, `database/migracao_historico_posicao_fila.sql` e `database/migracao_reinscricao_lista_espera.sql`.

### 3. Configurar as variáveis

Copie `.env.example` para `.env` e informe os dados do seu ambiente. O arquivo `.env` não deve ser enviado ao Git.

| Variável | Finalidade | Padrão ou exemplo |
| --- | --- | --- |
| `DB_URL` | URL JDBC do Oracle | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USER` | Usuário do Oracle, normalmente o RM | — |
| `DB_PASSWORD` | Senha do Oracle | — |
| `JWT_SECRET` | Segredo para assinar os tokens; use pelo menos 32 caracteres | — |
| `IA_URL` | URL do serviço de previsão de risco | `https://vagaviva-ia.onrender.com` |
| `IA_TIMEOUT_MS` | Tempo limite da chamada à IA, em milissegundos | `2000` |
| `CORS_ORIGINS` | Origens permitidas para o front, separadas por vírgula | `http://localhost:5173` |
| `OFERTA_VERIFICACAO_MS` | Intervalo de verificação de ofertas expiradas | `300000` |

Se a IA estiver indisponível ou exceder o tempo limite, a consulta ainda pode ser criada com risco `NAO_CALCULADO`.

### 4. Iniciar

No IntelliJ ou VS Code, abra `VagaVivaApplication.java` e use **Run/Executar**. No terminal, também é possível iniciar com:

```bash
mvn spring-boot:run
```

A API usa a porta `8080` por padrão. A rota raiz `GET /` retorna o estado do serviço.

### Executar com Docker

```bash
docker build -t vagaviva-api .
docker run --rm -p 8080:8080 \
  -e DB_URL="$DB_URL" \
  -e DB_USER="$DB_USER" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e JWT_SECRET="$JWT_SECRET" \
  -e IA_URL="${IA_URL:-https://vagaviva-ia.onrender.com}" \
  vagaviva-api
```

## Autenticação

As rotas internas exigem `Authorization: Bearer <token>`. Obtenha o token por `POST /auth/login`. As rotas públicas são a raiz `/`, as rotas de login, ativação e consulta de convite, além das rotas sob `/publico/`.

O perfil `GESTAO` pode administrar os usuários da equipe. As rotas públicas para pacientes usam um token aleatório no link e não exigem login.

Para entrar no ambiente de demonstração, execute `database/senhas_demo.sql`. A coleção `insomnia_vagaviva.json` já inclui as requisições **Login (gestão)** e **Login (recepção)** e usa as variáveis `base_url`, `senha` e `token`.

No ambiente do Insomnia, configure `base_url` como `https://vagaviva-api.onrender.com` (ou `http://localhost:8080` para a API local) e `senha` como `vagaviva2026`. As contas de demonstração são:

| Perfil | E-mail |
| --- | --- |
| Gestão | `marcos.andrade@saude.sp.gov.br` |
| Recepção | `regina.costa@saude.sp.gov.br` |

### Login no Insomnia e reutilização do token

1. Abra a requisição **Login (gestão)** ou **Login (recepção)** da coleção. Ela envia `POST {{ _.base_url }}/auth/login` com este JSON:

   ```json
   {
     "email": "marcos.andrade@saude.sp.gov.br",
     "senha": "{{ _.senha }}",
     "manterConectado": true
   }
   ```

   Para recepção, use `regina.costa@saude.sp.gov.br` e `"manterConectado": false`; essa requisição já está pronta na coleção.

2. Envie a requisição. Na resposta `200`, copie o valor do campo `token`.
3. Abra **Manage Environments → Base Environment** e cole esse valor em `token`, sem aspas e sem escrever `Bearer`.
4. Salve o ambiente. As requisições protegidas da coleção já usam `{{ _.token }}` como Bearer Token; não é necessário fazer login de novo para cada rota.

A sessão de gestão com `manterConectado: true` dura até 30 dias; a sessão de recepção com `false` dura até 8 horas. Depois do vencimento, repita o login e atualize a variável `token`. Essas credenciais são somente para demonstração e não devem ser reutilizadas em ambientes reais.

## Endpoints principais

As rotas recebem e devolvem JSON. Datas usam `AAAA-MM-DD`; data e hora usam `AAAA-MM-DDTHH:mm:ss`.

| Recurso | Rotas |
| --- | --- |
| Saúde da API | `GET /` |
| Autenticação | `POST /auth/login` · `GET /auth/me` · `POST /auth/logout` · `GET /auth/convite/{codigo}` · `POST /auth/ativar` |
| Usuários da equipe | `/usuarios` e `/usuarios/{id}`; ações de convite, desativação e reativação (perfil `GESTAO`) |
| Unidades | CRUD em `/unidades` · `GET/POST /unidades/{id}/lista-espera` · `GET /unidades/{id}/profissionais` |
| Especialidades | CRUD em `/especialidades` |
| Profissionais | CRUD em `/profissionais` · consultas e horários livres em `/profissionais/{id}/consultas` e `/profissionais/{id}/horarios-livres?data=AAAA-MM-DD` |
| Pacientes | CRUD em `/pacientes` · busca por consultas e fila · agendamento em `/pacientes/{id}/consultas` |
| Consultas | CRUD em `/consultas` · confirmação, cancelamento, liberação, presença e explicação de risco em `/consultas/{id}/...` |
| Lista de espera | CRUD em `/lista-espera` · classes em `/lista-espera/classes` · aceitar e recusar ofertas |
| Painel | `GET /painel?unidade=1&inicio=AAAA-MM-DD&fim=AAAA-MM-DD` |
| Área pública do paciente | `/publico/consultas/{token}`, `/publico/fila/{token}` e ações correspondentes |

Para criar uma consulta, use `POST /pacientes/{id}/consultas`. Para consultar horários livres, use `GET /profissionais/{id}/horarios-livres?data=AAAA-MM-DD`.

### Fluxo de devolução de vaga

| Método e rota | Resultado |
| --- | --- |
| `PATCH /consultas/{id}/cancelar` | Devolve a vaga e tenta oferecê-la ao próximo pedido compatível |
| `PATCH /consultas/{id}/liberar` | A recepção devolve uma vaga não confirmada a menos de 24 horas da consulta |
| `PATCH /lista-espera/{id}/aceitar` | A consulta oferecida passa ao paciente e fica confirmada |
| `PATCH /lista-espera/{id}/recusar` | O pedido permanece na fila e a vaga pode ser oferecida ao próximo |
| `GET /publico/fila/{token}` | Consulta pública da posição, etapa e eventual oferta |

O cancelamento e a tentativa de oferta são feitos na mesma transação. Se não houver pedido compatível, a resposta da ação traz `oferta: null`.

## Regras de negócio principais

- Um profissional não pode ter duas consultas ativas no mesmo horário. Consultas canceladas ou liberadas não bloqueiam o horário.
- A data do agendamento deve ser futura e o profissional precisa atender a especialidade selecionada.
- O risco de falta é informativo: não cancela nem impede consultas.
- A IA estima o risco com base nos dados da consulta e no histórico do paciente. Se estiver indisponível, o agendamento continua com risco `NAO_CALCULADO`.
- A vaga devolvida é oferecida a um paciente compatível com unidade e especialidade; a preferência por profissional e a disponibilidade para vaga no mesmo dia também são consideradas.
- A fila prioriza prazo legal, classe de risco, tempo de espera e critérios de desempate definidos pelo projeto.
- Um paciente não pode ter dois pedidos ativos para a mesma especialidade, mas pode voltar à fila após um pedido atendido ou desistido.

## Respostas de erro

Os erros seguem um formato JSON comum com `status`, `erro`, `mensagem`, `caminho`, `timestamp` e `campos`.

| Situação | HTTP |
| --- | ---: |
| Corpo inválido ou campos obrigatórios ausentes | 400 |
| Token ausente ou inválido | 401 |
| Perfil sem permissão | 403 |
| Recurso não encontrado | 404 |
| Método não permitido na rota | 405 |
| Regra de negócio violada ou conflito de horário | 409 |
| Muitas tentativas de login | 429 |
| Falha inesperada | 500 |

## Insomnia

Importe `insomnia_vagaviva.json` no Insomnia. Configure as variáveis do ambiente, faça login para obter o token e use-o como Bearer Token nas rotas protegidas. Os exemplos cobrem consultas, pacientes, fila, painel e erros comuns.

## Limitações do MVP

- O sistema gera convites, mas não envia e-mail, WhatsApp ou SMS automaticamente; o contato é feito pela equipe.
- A operação de aceitar uma oferta transfere a consulta devolvida para o novo paciente. O histórico de ofertas é mantido separadamente no banco.
