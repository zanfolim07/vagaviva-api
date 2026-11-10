# Vaga Viva · API Java

API RESTful da **Vaga Viva**, solução da Global Solution 2026/2 (tema CitizenTech) da FIAP.

A Vaga Viva prevê quais consultas do SUS têm mais chance de falta e, quando uma vaga é devolvida, oferece-a ao próximo da lista de espera, pela gravidade do caso e pelo tempo de espera. Esta API guarda os dados no Oracle da FIAP, aplica as regras de negócio, consulta a IA no agendamento e é consumida pelo front-end em React.

## Integrantes (turma 1TDSPV)

| Nome | RM |
| --- | --- |
| Fabio Cezare Almeida | RM572642 |
| Guilherme Zanfolim Nunes Farias | RM570983 |
| Jaime Ringel | RM562044 |
| Lucas Monteiro Dias da Costa | RM571388 |
| Mateus Magalhães Romão de Moraes | RM573371 |

## Links

- **API no Render:** `[COLOCAR A URL DO RENDER]`
- **Vídeo (YouTube, não listado):** `[COLOCAR O LINK DO VÍDEO]`
- **API de IA (Python/Flask):** https://vagaviva-ia.onrender.com

No plano gratuito do Render, depois de um período sem uso, a primeira requisição pode levar até 2 minutos para responder.

## Tecnologias

Java 21, Spring Boot 3.3, JDBC puro com `PreparedStatement` e `DataSource` (sem JPA, Hibernate ou Spring Data), Oracle, Bean Validation, ModelMapper, JWT, BCrypt, Docker e Render.

## Estrutura

```text
src/main/java/br/com/fiap/vagaviva
├── model        Entidades do domínio e regra de ordem da fila
├── dao          Acesso ao Oracle com JDBC, um DAO por entidade
├── dto          Entrada (com validações) e saída da API
├── service      Regras de negócio
├── controller   Rotas REST e status HTTP
├── client       Chamada à API de IA
├── exception    Exceções do domínio
├── handler      Erros em JSON padronizado
└── config       CORS, ModelMapper e autenticação
database/        Scripts SQL do banco
```

## Como rodar

**1. Banco:** no SQL Developer, rode `database/VagaViva_BancoDeDados.sql` e depois `database/senhas_demo.sql` e `database/dados_demo.sql`.

**2. Variáveis de ambiente:** nenhuma senha fica no código.

| Variável | Para que serve |
| --- | --- |
| `DB_URL` | Endereço do Oracle: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USER` | Usuário do banco (RM) |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Segredo dos tokens, com 32 caracteres ou mais |
| `CORS_ORIGINS` | Origens do front, separadas por vírgula |
| `IA_URL` | Opcional. O padrão é https://vagaviva-ia.onrender.com |

**3. Executar:**

```bash
mvn spring-boot:run
```

Ou com Docker:

```bash
docker build -t vagaviva-api .
docker run -p 8080:8080 -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... -e JWT_SECRET=... vagaviva-api
```

A API sobe em `http://localhost:8080` e responde `{"api":"Vaga Viva","status":"ok",...}`.

**4. Testar:** importe `insomnia_vagaviva.json` no Insomnia, preencha a variável `senha` do ambiente com a senha de demonstração, faça o login em **Auth > Login** e cole o `token` na variável `token`. O comando `mvn test` roda os testes das regras, sem banco.

## Deploy

Web Service no Render, a partir do `Dockerfile` multi-stage da raiz, com as variáveis acima cadastradas no painel do serviço.

## Endpoints

As rotas usam JSON. Exceto `GET /`, `POST /auth/login`, `POST /auth/ativar`, `GET /auth/convite/{codigo}` e `/publico/**`, todas exigem `Authorization: Bearer <token>`.

| Recurso | Rotas |
| --- | --- |
| Login | `POST /auth/login` · `GET /auth/me` · `POST /auth/logout` |
| Equipe | `/usuarios` e `/usuarios/{id}` (perfil GESTAO) |
| Unidades | `/unidades` e `/unidades/{id}` · `GET /unidades/{id}/lista-espera` e `/profissionais` |
| Especialidades | `/especialidades` e `/especialidades/{id}` |
| Profissionais | `/profissionais` e `/profissionais/{id}` · `GET /profissionais/{id}/consultas` |
| Pacientes | `/pacientes` e `/pacientes/{id}` · `GET/POST /pacientes/{id}/consultas` · `GET /pacientes/{id}/lista-espera` |
| Consultas | `GET /consultas` · `GET/PUT/DELETE /consultas/{id}` · `PATCH /consultas/{id}/confirmar`, `/cancelar`, `/liberar` e `/presenca` |
| Lista de espera | `/lista-espera` e `/lista-espera/{id}` · `PATCH /lista-espera/{id}/aceitar` e `/recusar` |
| Painel | `GET /painel` |
| Paciente, sem login | `/publico/consultas/{token}` e `/publico/fila/{token}` |

As rotas completas, os exemplos de JSON, as regras de negócio, as validações e os acessos de demonstração estão na documentação técnica em PDF, enviada com a entrega.

## Regra de negócio principal

Um profissional não pode ter duas consultas no mesmo horário (409). Quando uma consulta é cancelada, a vaga é oferecida ao próximo paciente compatível da fila, na mesma transação JDBC. O risco de falta só orienta o contato: a IA nunca cancela nem nega uma consulta.

## Erros

Todo erro volta em JSON, sem stack trace e sem SQL, com `status`, `erro`, `mensagem`, `caminho`, `timestamp` e `campos`.

| Situação | Status |
| --- | --- |
| Dados inválidos ou JSON malformado | 400 |
| Sem token ou token expirado | 401 |
| Perfil sem permissão | 403 |
| Id ou rota inexistente | 404 |
| Método não suportado | 405 |
| Regra de negócio violada ou registro com dependentes | 409 |
| Tentativas demais de login | 429 |
| Erro inesperado | 500 |

## Uso de IA generativa

O grupo usou o Claude (Anthropic) como apoio para revisar o código, explicar erros e organizar a documentação. Todo o código foi revisado, testado e entendido pelos integrantes.

## Limitações do MVP

A API gera os convites e os links do paciente, mas não envia e-mail, WhatsApp ou SMS: o envio é feito pela equipe.
