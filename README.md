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

- **API no Render:** https://vagaviva-api.onrender.com
- **Vídeo (YouTube, não listado):** `[COLOCAR O LINK DO VÍDEO]`
- **API de IA (Python/Flask):** https://vagaviva-ia.onrender.com

No plano gratuito do Render, depois de um período sem uso, a primeira requisição pode levar até 2 minutos para responder.

## Tecnologias

O professor de Front vai precisar do login para abrir as telas internas, e o zip dele não leva o PDF técnico. Por isso, informe o e-mail e a senha de demonstração no comentário da entrega no Portal, se houver esse campo. Outra opção é acrescentar a seção de acesso só no README da cópia que vai no zip. Assim a senha não fica no GitHub público.                                                     
✻ Worked for 38s · done 10:36 PM                                                                
❯ nao precisa falar do email e senha pois a gente coloca no readme depois n tem problema so nao meu nome e rm

  Ran 1 shell command                                                                           
❯ no caso email e rm do banco                                                                   
Entendido: o RM e a senha de acesso ao banco não vão para o GitHub. Conferi o Java, o front e nenhum deles tem esses dados de cone

- Usuário e senha do Oracle: ficam só nas variáveis de ambiente do Render, que são privadas.  - application.properties: lê ${DB_UShum valor escrito.
- .env.example: tem só os textos de exemplo seu_rm_aqui e sua_senha_aqui.                     - Arquivo .env: não existe nos projeria se você criasse um.
- E-mails nos scripts SQL: são contas fictícias da demonstração (@saude.sp.gov.br), nenhum é seu.

O seu RM aparece apenas na lista de integrantes dos READMEs, nos integrantes.txt, na página Integrantes do front e nos notebooks da IA. Essa lista é obrigatória no enunciado, então deve continuar. Oé o mesmo do login do Oracle, mas seo banco.

Pode fazer o push dos dois READMEs e seguir para a Vercel, com VITE_API_URL =                 https://vagaviva-api.onrender.com.

✻ Churned for 22s · done 10:39 PM

❯ me mande o readme n0ovamente                                                                  
Seguem os dois READMEs, sem dados de acesso ao banco. O do front está sem a seção de login de demonstração, que vocês vão colocar

vagaviva-api/README.md

# Vaga Viva · API Java

API RESTful da **Vaga Viva**, solução da Global Solution 2026/2 (tema CitizenTech) da FIAP.

A Vaga Viva prevê quais consultas do SUS têm mais chance de falta e, quando uma vaga é devolvida,
oferece-a ao próximo da lista de esp pelo tempo de espera. Esta API guarda os dados no Oracle da FIAP, aplica as regras de negócio, consulta a IA no agendamento e é consumida pelo front-end em React.

## Integrantes (turma 1TDSPV)

| Nome | RM |
| --- | --- |
| Fabio Cezare Almeida | RM572642 |
| Guilherme Zanfolim Nunes Farias | RM570983 |
| Jaime Ringel | RM562044 |
| Lucas Monteiro Dias da Costa | RM571388 |
| Mateus Magalhães Romão de Moraes |

## Links

- **API no Render:** https://vagaviva-api.onrender.com
- **Vídeo (YouTube, não listado):** `[COLOCAR O LINK DO VÍDEO]`
- **API de IA (Python/Flask):** http

No plano gratuito do Render, depois eira requisição pode levar até 2minutos para responder.

## Tecnologias

Java 21, Spring Boot 3.3, JDBC puro taSource` (sem JPA, Hibernate ou
Spring Data), Oracle, Bean ValidatioDocker e Render.

## Estrutura

```text
src/main/java/br/com/fiap/vagaviva
├── model        Entidades do domínio e regra de ordem da fila
├── dao          Acesso ao Oracle co
├── dto          Entrada (com validações) e saída da API
├── service      Regras de negócio
├── controller   Rotas REST e status HTTP
├── client       Chamada à API de IA
├── exception    Exceções do domínio
├── handler      Erros em JSON padronizado
└── config       CORS, ModelMapper e
database/        Scripts SQL do banco
```

## Como rodar

**1. Banco:** no SQL Developer, rodedos.sql` e depois`database/senhas_demo.sql` e `database/dados_demo.sql`.

**2. Variáveis de ambiente:** nenhuma senha fica no código.

| Variável | Para que serve |
| --- | --- |
| `DB_URL` | Endereço do Oracle: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USER` | Usuário do banco (RM)
| `DB_PASSWORD` | Senha do banco |                                                                   | `JWT_SECRET` | Segredo dos tokens,
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
**4. Testar:** importe `insomnia_vagaviva.json` no Insomnia, preencha a variável `senha` do ambiente com a senha de demonstração, faça o ole o `token` na variável `token`. Ocomando `mvn test` roda os testes das regras, sem banco.

## Deploy

Web Service no Render, a partir do `Dockerfile` multi-stage da raiz, com as variáveis acima cadastradas no painel do serviço.

## Endpoints

As rotas usam JSON. Exceto `GET /`, `POST /auth/login`, `POST /auth/ativar`, `GET
/auth/convite/{codigo}` e `/publico/ion: Bearer <token>`.

| Recurso | Rotas |
| --- | --- |
| Login | `POST /auth/login` · `GET /auth/me` · `POST /auth/logout` |
| Equipe | `/usuarios` e `/usuarios/{id}` (perfil GESTAO) |
| Unidades | `/unidades` e `/unidades/{id}` · `GET /unidades/{id}/lista-espera` e `/profissionais` |
| Especialidades | `/especialidades`
| Profissionais | `/profissionais` e `/profissionais/{id}` · `GET /profissionais/{id}/consultas` |
| Pacientes | `/pacientes` e `/pacientes/{id}` · `GET/POST /pacientes/{id}/consultas` · `GET /pacientes/{id}/lista-espera` |
| Consultas | `GET /consultas` · `GET/PUT/DELETE /consultas/{id}` · `PATCH /consultas/{id}/confirmar`/cancelar`, `/liberar` e `/presenca| Lista de espera | `/lista-espera` CH /lista-espera/{id}/aceitar` e`/recusar` |
| Painel | `GET /painel` |
| Paciente, sem login | `/publico/consultas/{token}` e `/publico/fila/{token}` |                    
As rotas completas, os exemplos de JSON, as regras de negócio, as validações e os acessos de        demonstração estão na documentação tentrega.
                                                                                                    ## Regra de negócio principal

Um profissional não pode ter duas consultas no mesmo horário (409). Quando uma consulta é cancelada, a vaga é oferecida ao próximo paciente compatível da fila, na mesma transação JDBC. O risco de falta sorienta o contato: a IA nunca cancel
                                                                                                    ## Erros

Todo erro volta em JSON, sem stack trace e sem SQL, com `status`, `erro`, `mensagem`, `caminho`, `timestamp` e `campos`.
                                                                                                    | Situação | Status |
| --- | --- |                                                                                       | Dados inválidos ou JSON malformado
| Sem token ou token expirado | 401 |                                                               | Perfil sem permissão | 403 |
| Id ou rota inexistente | 404 |
| Método não suportado | 405 |                                                                      | Regra de negócio violada ou regist
| Tentativas demais de login | 429 |
| Erro inesperado | 500 |                                                                           
## Uso de IA generativa                                                                             
O grupo usou o Claude (Anthropic) como apoio para revisar o código, explicar erros e organizar a    documentação. Todo o código foi revios integrantes.

## Limitações do MVP                                                                                
A API gera os convites e os links do paciente, mas não envia e-mail, WhatsApp ou SMS: o envio é feito pela equipe.

vagaviva-front/README.md                                                                            
# Vaga Viva                                                                                         
Front-end da **Vaga Viva**, solução da Global Solution 2026/2 (tema CitizenTech) da FIAP, turma 1TDS
## Objetivo                                                                                         
Reduzir o desperdício de consultas no SUS. A Vaga Viva prevê quais consultas têm mais chance de falta, ajuda a recepção a priorizar o contato e, quando uma vaga é devolvida, oferece o horário ao próximo paciente compatível da lista de espencela nem nega uma consulta.

Este projeto é a interface: todos os dados que mudam vêm da API REST em Java, por meio da Fetch API.

## Tecnologias

- React 19, Vite 8 e TypeScript
- Tailwind CSS 4
- React Router 7
- React Hook Form
- Fetch API, sem Axios nem outra biblioteca de requisições

## Estrutura de pastas

```text
src/
├── assets/       Fotos dos integrantes
├── components/   Componentes reutilizáveis (cabeçalhos, formulários, selos, modal)
├── context/      Sessão do usuário
├── data/         Conteúdo fixo (integrantes, perguntas, municípios)
├── hooks/        useAsync e useRelo
├── pages/        Páginas institucionais e páginas da solução
├── routes/       Rotas e proteção por perfil
├── services/     Chamadas à API (http.ts e api.ts)
├── types/        Tipos dos dados da
└── utils/        Formatação de datas, telefones e valores
```

## Rotas

| Tipo | Rotas |
| --- | --- |
| Institucionais | `/`, `/integranteto` |
| Acesso | `/login`, `/primeiro-acesso` |
| Solução, com login | `/agenda`, `/consultas/nova`, `/consultas/:id`, `/pacientes`, `/lista-espera`, `/painel`, `/admin/equipe` |
| Paciente, com link | `/celular/:to

## Integrantes

| Nome | RM | Turma | GitHub | Linke
| --- | --- | --- | --- | --- |
| Fabio Cezare Almeida | 572642 | 1TDSPV | [FabioCAlmeida](https://github.com/FabioCAlmeida) | [Perfil](https://www.linkedin.com/in/fabio-cezare-almeida-4b448a408) |
| Guilherme Zanfolim Nunes Farias | 570983 | 1TDSPV | [zanfolim07](https://github.com/zanfolim07) |
[Perfil](https://www.linkedin.com/in) |
| Jaime Ringel | 562044 | 1TDSPV | [jaimeringel004](https://github.com/jaimeringel004) |
[Perfil](https://www.linkedin.com/in
| Lucas Monteiro Dias da Costa | 571388 | 1TDSPV | [monteiroo5](https://github.com/monteiroo5) | [Perfil](https://www.linkedin.com/in/lucas-monteiro-1110703b5/) |
| Mateus Magalhães Romão de Moraes | 573371 | 1TDSPV | [MagalhaesMatheus007](https://github.com/MagalhaesMatheus007) |
[Perfil](https://www.linkedin.com/in

## Como executar

Requer Node.js 20 ou superior.

```bash
npm install
cp .env.example .env
npm run dev
```

No Windows (PowerShell), troque o `cp` por `Copy-Item .env.example .env`. No arquivo `.env`, defina `VITE_API_URL` com o endereço da API. A aplicação abre em `http://localhost:5173`.

Para gerar a versão de produção: `npm run build`.

## Links

- **GitHub:** tps://github.com/zanfolim07/vagaviva-api.git
- **Vercel:** 
- **API Java (Render):** https://vagaviva-api.onrender.com
- **Vídeo no YouTube:** 
