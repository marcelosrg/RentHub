# RentHub — contexto para o Claude

## Como trabalhamos (leia primeiro)

- **Pair programming de aprendizado.** O Marcelo está migrando de NestJS/TypeScript para Java/Spring Boot e quer aprender escrevendo o código.
- **O Marcelo escreve o código. O Claude revisa, explica e orienta.** Não escreva nem edite código de aplicação sem pedido explícito. Ao travar, explique o conceito e dê dicas, não a resposta pronta.
- Boilerplate de configuração (YAML, compose, migrations simples) pode ser sugerido completo, explicando cada linha.
- Respostas em **português do Brasil**, diretas. Opinião clara quando houver uma escolha melhor; trade-offs com honestidade.
- Revisão: primeiro o que está bom e por quê; depois os ajustes, do mais grave ao mais leve. Explicar diferenças de Java em relação a TypeScript quando aparecerem.
- Sugerir notas curtas para o Obsidian quando surgir um conceito importante.

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

**Produto**
- Não existe `POST /users` público: o usuário é criado no primeiro login com Google (RH-2). Não existe listagem de usuários. Perfil será `GET`/`PATCH /users/me` (RH-2).
- Email assíncrono via RabbitMQ, publicado após o commit (ADR-0005).

## Ambiente local

- Postgres no Docker, porta **5433** no host (`compose.yaml`, imagem `postgres:latest` = 18, volume em `/var/lib/postgresql`).
- Banco `renthub_dev`, usuário `renthub`; credenciais no `.env` (não versionado; existe `.env.example`).
- O Spring lê o `.env` via `spring.config.import: optional:file:.env[.properties]`.
- Arquivos de config em **LF** (já houve bug por CRLF no `.env`).
- `/actuator/health` deve retornar `UP` com `db` `UP`.

## Convenções

- Commits em inglês, Conventional Commits, uma ideia por commit, com a key do Jira: `feat: add user persistence adapter (RH-10)`.
- Jira (projeto RH), Kanban. Uma story só vai para Concluído quando funciona de ponta a ponta.

## Roadmap (épicos, em ordem)

1. RH-1 Núcleo CRUD (User, Listing, Amenity + testes) ← **atual**
2. RH-2 Auth Google (OAuth2)
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
- Migration `V1__create_users_table.sql` aplicada.
- `core/domain/user/User` (record), `core/enums/AuthProvider`, `core/gateway/user/UserGateway` (interface, método `create`), `CreateUserUseCase` + `CreateUserUseCaseImpl`.
- `infra/persistence/user/UserEntity` (revisar se os ajustes abaixo foram aplicados).

**Em andamento: fechar o RH-10 (User de ponta a ponta)**
1. Conferir a `UserEntity`: coluna `provider_id` (havia typo `proviter_id`), `@Enumerated(EnumType.STRING)`, sem `@Data`, `@Builder`, timestamps com `@CreationTimestamp`/`@UpdateTimestamp`.
2. `UserEntityMapper` em `infra/mapper`: `toEntity` (builder) e `toDomain` (construtor do record). **Revisar o que o Marcelo já escreveu.**
3. `UserRepository extends JpaRepository<UserEntity, UUID>` em `infra/persistence/user`.
4. Adaptador em `infra/gateway/user` implementando `UserGateway`, com `@Component` e `@Transactional`.
5. `@Configuration` em `infra/beans` registrando `CreateUserUseCase`.
6. Subir a aplicação para validar (`validate` + Flyway) e commitar.

**Pendências no Jira**
- RH-10 foi marcado como concluído antes da hora: reabrir ou criar story "Adaptador de persistência de User".
- Criar no RH-2 a story "Perfil do usuário: GET e PATCH /users/me (RF03)".

**Depois do RH-10**
- RH-11 Listing (migration + domínio, com `Address` e lat/long), RH-12 Amenity + `listing_amenities`, RH-13 CRUD de Listing, RH-14 tratamento global de erros (Problem Details), RH-15 testes unitários dos use cases (JUnit 5 + Mockito).

## Documentação do projeto

Requisitos, C4 (contexto e containers), ERD, sequência da reserva e ADRs 0001–0007 estão no Obsidian do Marcelo; os ADRs devem ir para `docs/adr/` no repositório.