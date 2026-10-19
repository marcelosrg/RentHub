# RentHub — contexto para o Claude

## Como trabalhamos (leia primeiro)

- **Pair programming de aprendizado.** O Marcelo está migrando de NestJS/TypeScript para Java/Spring Boot e quer aprender escrevendo o código.
- **O Marcelo escreve o código. O Claude revisa, explica e orienta.** Não escreva nem edite código de aplicação sem pedido explícito. Ao travar, explique o conceito e dê dicas, não a resposta pronta.
- Boilerplate de configuração (YAML, compose, migrations simples) pode ser sugerido completo, explicando cada linha.
- Respostas em **português do Brasil**, diretas. Opinião clara quando houver uma escolha melhor; trade-offs com honestidade.
- Revisão: primeiro o que está bom e por quê; depois os ajustes, do mais grave ao mais leve. Explicar diferenças de Java em relação a TypeScript quando aparecerem.
- Sugerir notas curtas para o Obsidian quando surgir um conceito importante.
- **Um passo por vez.** Dar só o passo atual e esperar o Marcelo fazer antes de apresentar o próximo. Nada de empilhar vários passos, revisões e "próximas etapas" na mesma resposta. Pedido pontual ("só cria a msg de commit") → responder só aquilo.
- Quando o Marcelo pedir explicitamente para ver o código pronto ("me manda o código pra eu aprender"), mostrar no chat com explicação; ele mesmo digita nos arquivos.

## O projeto

API de reservas de imóveis por temporada, estilo Airbnb: o hóspede escolhe check-in e check-out e reserva. **Só temporada** (aluguel longo e venda fora do MVP, ADR-0006). **Só API**, sem frontend (ADR-0004); demo via Swagger.

Objetivo: portfólio para vaga Java/Spring pleno. O Marcelo quer começar a aplicar para vagas já, com o projeto em andamento.

**Diferencial técnico:** reserva sem overbooking sob concorrência (lock pessimista e/ou constraint de exclusão com `daterange(check_in, check_out, '[)')`), testada com Testcontainers.

## Stack

- Java 21, Spring Boot 4.1.1 (starters novos: `spring-boot-starter-webmvc`, `-flyway` etc.)
- Maven com wrapper fixado em **3.9.11** (a 3.10 quebrava o importador do IntelliJ)
- PostgreSQL via Docker, Flyway, Spring Data JPA, Validation, Actuator
- Lombok (permitido **só em `infra`**)
- Projeto roda dentro do WSL (Ubuntu 24.04); IntelliJ no Windows

## Arquitetura: Clean Architecture pura (ADR-0007)

```
com.renthub.api
├── core/                         ← Java puro: NADA de Spring, JPA, Jakarta ou Lombok
│   ├── domain/<modulo>/          ← entidades de domínio (records)
│   ├── enums/                    ← ex.: AuthProvider { GOOGLE }
│   ├── exception/
│   ├── gateway/<modulo>/         ← INTERFACES (portas de saída), ex.: UserGateway
│   └── usecases/<modulo>/        ← interface + Impl, injeção por construtor, campos final
└── infra/
    ├── beans/                    ← @Configuration registrando use cases como @Bean
    ├── config/                   ← demais configs (CORS, Jackson, Swagger)
    ├── dtos/
    ├── gateway/<modulo>/         ← implementações das portas (adaptadores), com @Component e @Transactional
    ├── mapper/                   ← domínio ⇄ entidade JPA / DTO
    ├── persistence/<modulo>/     ← entidades JPA (@Entity) + repositórios Spring Data
    └── presentation/             ← controllers
```

**Regras que toda revisão deve vigiar:**
- Nenhum import de `infra`, Spring, JPA ou Lombok dentro de `core`.
- Use cases sem `@Service`/`@Transactional`; registrados em `infra/beans`. Transação fica no adaptador.
- `core/gateway` são **interfaces**; implementação só em `infra/gateway`.
- Um módulo não acessa a persistência de outro diretamente (monólito modular, ADR-0001).
- Pacotes em minúsculo; campos e variáveis em camelCase (snake_case só no banco).

## Decisões já tomadas

**Domínio e banco**
- IDs `UUID` (ADR-0002); no banco `DEFAULT gen_random_uuid()`, no JPA `@GeneratedValue(strategy = GenerationType.UUID)`.
- Dinheiro em `BigDecimal` / `NUMERIC(10,2)`, nunca `double`.
- Enums como `VARCHAR` + `CHECK` no banco e `@Enumerated(EnumType.STRING)` no JPA (ADR-0003). Nunca ORDINAL.
- Timestamps: `Instant` no Java, `TIMESTAMPTZ` no banco, UTC. Datas de calendário (check-in/out): `LocalDate` / `DATE`.
- Período da reserva é `[check_in, check_out)`. Preço da diária congelado na reserva (`nightly_price`).
- Constraints com nome no SQL (`uk_users_email`, `ck_users_auth_provider`).
- Entidade do anúncio se chama `Listing`.
- Usuário OAuth identificado por `(auth_provider, provider_id)`.

**JPA / Lombok**
- Nas entidades JPA: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`. **Não usar `@Data`** (equals/hashCode/toString problemáticos em entidade).
- `created_at` com `@CreationTimestamp` + `updatable = false`; `updated_at` com `@UpdateTimestamp`.
- Mappers: `toEntity` usa o builder; `toDomain` usa o construtor do record (atenção à ordem).
- `ddl-auto: validate` (schema só via Flyway) e `open-in-view: false`.
- Migration aplicada não se edita: correção vira nova versão.
- `@Transactional` sempre de `org.springframework.transaction.annotation` (não o do Jakarta); leituras com `readOnly = true`.
- Repositórios Spring Data **com** `@Repository`: é redundante, mas o Marcelo decidiu manter para deixar explícito. Não sugerir remover.

**Erros e validação**
- Exceções de domínio em `core/exception`, hierarquia por categoria: `DomainException` (abstract) → `AlreadyExistsException` (abstract, 409) → específicas (`UserAlreadyExistsException`). `NotFoundException` (404) e `BusinessRuleException` (422) só nascem no primeiro uso. Core nomeia pelo negócio, nunca por status HTTP.
- `infra/presentation/handler/GlobalExceptionHandler` (`@RestControllerAdvice`) trata **categorias**, devolvendo `ProblemDetail` (RFC 9457). Validação → 400 com `errors` (campo → mensagem); enum inválido no JSON → 400 com valores aceitos; `DataIntegrityViolationException` → 409 genérico (sem vazar SQL).
- **Não** ligar `spring.mvc.problemdetails.enabled`: o handler do Spring tem prioridade e engole os nossos. Padronização completa (estender `ResponseEntityExceptionHandler` + handler de `Exception` → 500 com log) é escopo do RH-14.
- Unicidade: use case checa (`existsBy...`) para mensagem clara; constraint do banco é a garantia final (janela de concorrência).
- DTOs separados por direção: `CreateXRequest` (entrada, com Bean Validation e `@Valid` no controller) e `XResponse` (saída).
- Spring Boot 4 usa **Jackson 3**: pacote `tools.jackson.*`, não `com.fasterxml.jackson.*`.

**Segurança (RH-2)**
- Starter do Boot 4: `spring-boot-starter-security-oauth2-client` (o nome antigo `spring-boot-starter-oauth2-client` é do Boot 3).
- Google registrado em `spring.security.oauth2.client.registration.google` com `scope: openid, profile, email` (OIDC; o `sub` do id_token é o `provider_id`). Credenciais `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` no `.env`.
- `SecurityConfig` em `infra/config/security`: `/actuator/health` e `/actuator/health/**` públicos, resto `authenticated()`, `oauth2Login(Customizer.withDefaults())`. Sintaxe só com lambda (o `.and()` foi removido no Security 7).
- **Até a RH-29 a autenticação é por sessão** (`JSESSIONID`): o fluxo OAuth2 guarda o `state` na sessão. Não usar `STATELESS` nem desligar CSRF antes do JWT; os dois entram juntos na RH-29 (`csrf(AbstractHttpConfigurer::disable)`).
- Efeito esperado até lá: `POST /api/v1/users` (dev) exige login e dá 403 por CSRF.
- Iniciar login: `GET /oauth2/authorization/google`. O callback `/login/oauth2/code/google` é chamado pelo Google, não se acessa direto. Com um só provedor, o Spring redireciona direto para o Google (não mostra `/login`).
- Sugestão em aberto: trocar `management.endpoint.health.show-details` de `always` para `when-authorized` (health público hoje expõe caminho do disco e detalhes do banco).

**Produto**
- Não existe `POST /users` público: o usuário é criado no primeiro login com Google (RH-2). Não existe listagem de usuários. Perfil será `GET`/`PATCH /users/me` (RH-2).
- Email assíncrono via RabbitMQ, publicado após o commit (ADR-0005).

## Ambiente local

- Postgres no Docker, porta **5431** no host (`compose.yaml` e `application.yaml`; antes era 5433), imagem `postgres:latest` = 18, volume em `/var/lib/postgresql`.
- Banco `renthub_dev`, usuário `renthub`; credenciais no `.env` (não versionado; existe `.env.example`).
- O Spring lê o `.env` via `spring.config.import: optional:file:.env[.properties]`.
- Arquivos de config em **LF** (já houve bug por CRLF no `.env`).
- `/actuator/health` deve retornar `UP` com `db` `UP`.

## Convenções

- Commits em inglês, Conventional Commits, uma ideia por commit, **sem a key do Jira** (decisão do Marcelo): `feat: add user persistence adapter`. Não sugerir `(RH-xx)`.
- Jira (projeto RH, site `marcelohrs2005.atlassian.net`), Kanban. Transições do workflow: `A fazer`, `Fazendo` (→ Em andamento), `Feito`. Uma story só vai para Feito quando funciona de ponta a ponta. O Claude move as stories quando o Marcelo conclui.

## Roadmap (épicos, em ordem)

1. RH-1 Núcleo CRUD (User, Listing, Amenity + testes): User pronto; Listing/Amenity depois do RH-2
2. RH-2 Auth Google (OAuth2) ← **atual**
3. RH-3 Reserva + concorrência
4. RH-23 Escala horizontal (Nginx + várias instâncias)
5. RH-24 Réplica de leitura (primário/réplica)
6. RH-4 Upload S3 + Busca + Redis
7. RH-5 Mensageria + Observabilidade
8. RH-6 CI/CD (adiado por decisão do Marcelo)
9. RH-7 Chat (futuro)

## Status atual

**Pronto**
- RH-8 setup: API sobe e conecta no Postgres.
- RH-9 estrutura de pacotes `core`/`infra`.
- RH-10 User de ponta a ponta: migration V1, `User` (record), `UserEntity`, `UserRepository`, `UserEntityMapper`, `UserRepositoryGateway`, `CreateUserUseCase` registrado em `infra/beans`, `CreateUserRequest`/`UserResponse` + `UserDtoMapper`, `POST /api/v1/users` (**endpoint de dev**, remoção no RH-25).
- Regras de criação de User: email e `(auth_provider, provider_id)` únicos → `UserAlreadyExistsException` (409); email normalizado (`trim` + `toLowerCase(Locale.ROOT)`) no compact constructor do `User`; email exige domínio com ponto no DTO.
- Hierarquia de exceções de domínio + `GlobalExceptionHandler` com ProblemDetail (ver "Erros e validação").
- RH-26 OAuth Client no Google Cloud (projeto, tela de consentimento External/teste, client Web com redirect `http://localhost:8080/login/oauth2/code/google`).
- RH-27 Login com Google via Spring Security: `/oauth2/authorization/google` vai ao Google e volta autenticado; health público.

**Pendências pequenas** (juntar num commit `chore: remove unused imports and redundant annotations`)
- Imports de `LocalDateTime` sem uso em `UserEntity` e `UserResponse`.
- `@Repository` redundante no `UserRepository`; espaço em `providerId )` no `UserGateway`.
- `import ...Customizer;` sem uso no `SecurityConfig`.
- `.env.example` e alguns arquivos vieram em CRLF; considerar `* text=auto eol=lf` no `.gitattributes`.
- ADRs em `docs/ADRS/` com espaços e acentos no nome e sem título `#`; ADR-0007 descreve `domain/application/infrastructure/web` por módulo, mas o código é `core`/`infra` por camada (reescrever).

**Agora: RH-2 Auth Google** (antecipado para antes do Listing, decisão do Marcelo)
- RH-26 ✅ · RH-27 ✅
- **RH-28 Encontrar ou criar usuário no login: código e testes prontos (Jira ainda em Em andamento)**
  - ✅ `findByAuthProviderAndProviderId` → `Optional<User>` (gateway, repositório, adaptador com `readOnly = true`).
  - ✅ `FindOrCreateUserUseCase` + `Impl`: recebe `UserGateway` e `CreateUserUseCase` (composição, reaproveita a regra de email duplicado); `findBy...().orElseGet(() -> createUserUseCase.execute(user))`. Bean registrado no `BeanConfiguration`.
  - ✅ `infra/config/security/OAuth2LoginSuccessHandler` (`AuthenticationSuccessHandler`, assinatura de 3 parâmetros): `OidcUser` (pattern matching) → `User` (sub→providerId, email, fullName, picture) → use case. Resposta **temporária** `{"userId": "..."}` (vira o JWT no RH-29). Registrado no `SecurityConfig` com `.oauth2Login(oauth2 -> oauth2.successHandler(successHandler))`.
  - ✅ `FindOrCreateUserUseCaseImplTest`: usuário existente (`verify(createUserUseCase, never())` protege contra o bug do `orElse`) e usuário novo. Total: 10 testes unitários passando.
  - ⏭️ **Para fechar:** o Marcelo confirmar o teste manual dos dois logins (aba anônima em `/oauth2/authorization/google`: mesmo `userId` nas duas vezes, uma linha só em `users`). Confirmado → mover RH-28 para Feito e RH-29 para Fazendo no Jira.
  - Limitação conhecida: exceções lançadas no success handler (ex.: `UserAlreadyExistsException`) **não** passam pelo `@RestControllerAdvice` (rodam na cadeia de filtros do Security, fora do DispatcherServlet) → viram 500. Tratar no RH-29.
- **RH-29 JWT próprio da API ← próximo** (stateless; aqui entram `SessionCreationPolicy.STATELESS`, CSRF desligado, resource server validando o JWT, success handler devolvendo `{"accessToken": "..."}` e tratamento de erro no handler)
- RH-30 GET /users/me + RH-25 remover POST /users de dev
- RH-31 PATCH /users/me (cobre o antigo item "Perfil do usuário: GET e PATCH /users/me")

**Depois do RH-2: RH-11 Listing (migration + domínio)**
1. Modelar campos (o Marcelo lista campos/tipos/obrigatoriedade; comparar com o ERD do Obsidian).
2. `V2__create_listings_table.sql`: FK `host_id` → `users.id`, preço `NUMERIC(10,2)`, lat/long.
3. Domínio: record `Listing` + value object `Address`.
4. `ListingEntity` (JPA).
- Atenção ao monólito modular: Listing não acessa `UserRepository` direto (verificar host via porta/use case do módulo User).

**Depois**
- RH-12 Amenity + `listing_amenities`, RH-13 CRUD de Listing, RH-14 tratamento global de erros (completar). RH-15 (testes unitários) já está Feito: cada story nova inclui os próprios testes nos critérios de aceite.

## Aprendizado em andamento

- **Testes unitários:** o Marcelo pediu para aprender de verdade (quer saber responder em entrevista). Já foi explicado: definição (pequeno/isolado/rápido), pirâmide (unitário/integração com Testcontainers/E2E), mock vs stub vs spy vs fake, given/when/then, "um teste por caminho do código" (feliz, cada erro, bordas), `assertThat` (resultado) vs `verify` (interação), e perguntas de entrevista com respostas.
  - Exercício pendente: descrever os 2 testes do `FindOrCreateUserUseCaseImplTest` no formato "dado que... quando... então...".
  - Combinado: no RH-31 (PATCH /users/me) o **Marcelo escreve os testes sozinho**; o Claude só lista os cenários e revisa.
  - Oferecido: gerar nota de estudo de testes para o Obsidian (como a de tratamento de erros, que foi salva em `C:\Users\User\Downloads`).

## Documentação do projeto

ADRs 0001–0007 e o diagrama de sequência da reserva já estão em `docs/` no repositório. Requisitos (RF/RN/RNF), C4 (contexto e containers) e ERD ainda estão só no Obsidian do Marcelo.

## Trocando de máquina

- O `.env` não é versionado: recriar a partir do `.env.example` com as credenciais do Postgres e `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`. O secret só é mostrado na criação no Google Cloud; se não tiver cópia, gerar um novo secret no client `renthub-local`.
- Fazer `git push` antes de trocar de máquina (o `develop` local pode estar sem upstream: `git push -u origin develop`).