# RentHub — contexto para o Claude

## Como trabalhamos (leia primeiro)

- **Pair programming de aprendizado.** O Marcelo está migrando de NestJS/TypeScript para Java/Spring Boot e quer aprender escrevendo.
- **O Marcelo escreve o código. O Claude revisa, explica e orienta.** Não escreva nem edite código de aplicação sem ele pedir explicitamente. Ao travar, explique o conceito e dê dicas, não a resposta pronta.
- Boilerplate de configuração (YAML, compose, migrations simples) pode ser sugerido completo, sempre explicando cada linha.
- Respostas em **português do Brasil**, diretas, sem enrolação. Opinião clara quando houver uma melhor escolha; apontar trade-offs com honestidade.
- Evitar over-engineering, exceto onde foi decidido conscientemente (ver Clean Architecture abaixo).
- Ao revisar: apontar o que está bom e por quê, depois o que ajustar. Explicar diferenças de Java em relação a TypeScript quando aparecerem.

## O projeto

API de reservas de imóveis por temporada, estilo Airbnb: o hóspede escolhe check-in e check-out e reserva. Projeto de portfólio para vaga Java/Spring pleno.

- **Stack:** Java 21, Spring Boot 4.1, Maven (wrapper fixado em 3.9.11 por compatibilidade com o IntelliJ), PostgreSQL (Docker), Flyway.
- **Somente API** (sem frontend no MVP); demo via Swagger.
- **Diferencial técnico:** reserva sem overbooking sob concorrência (lock pessimista e/ou constraint de exclusão com `daterange '[)'`), testada com Testcontainers.

## Arquitetura (ADR-0007: Clean Architecture pura)

```
com.renthub.api
├── core/                     ← Java puro: NADA de Spring, JPA ou Jakarta
│   ├── domain/<modulo>/      ← entidades de domínio
│   ├── enums/
│   ├── exception/
│   ├── gateway/<modulo>/     ← INTERFACES (portas de saída)
│   └── usecases/<modulo>/    ← interface + Impl, injeção por construtor
└── infra/
    ├── beans/                ← @Configuration registrando os use cases como @Bean
    ├── config/               ← demais configs (CORS, Jackson, Swagger)
    ├── dtos/
    ├── gateway/<modulo>/     ← implementações das portas (adaptadores)
    ├── mapper/
    ├── persistence/<modulo>/ ← entidades JPA (@Entity) + Spring Data
    └── presentation/         ← controllers
```

**Regras que a revisão deve vigiar:**
- Nenhum import de `infra` ou de framework dentro de `core`.
- Use cases sem `@Service`/`@Transactional`; são registrados em `infra/beans`. Transações ficam nos adaptadores.
- Um módulo não acessa a persistência de outro diretamente (monólito modular, ADR-0001).
- Pacotes em minúsculo.

## Decisões já tomadas

- IDs `UUID` (ADR-0002). Dinheiro em `BigDecimal` / `NUMERIC(10,2)`, nunca `double`.
- Enums como `VARCHAR` + `CHECK`, mapeados com `EnumType.STRING` (ADR-0003).
- Entidade do anúncio se chama `Listing`.
- Datas de reserva como `DATE`; timestamps `TIMESTAMPTZ` em UTC. Período da reserva é `[check_in, check_out)`.
- Preço da diária congelado na reserva (`nightly_price`).
- `spring.jpa.hibernate.ddl-auto=validate`; schema só via Flyway. `open-in-view=false`.
- Email assíncrono via RabbitMQ, publicado após o commit (ADR-0005).
- Usuário OAuth identificado por `(auth_provider, provider_id)`.

## Ambiente local

- Postgres no Docker na porta **5433** do host (`compose.yaml`, imagem `postgres:latest` = 18; volume em `/var/lib/postgresql`).
- Credenciais no `.env` (não versionado; existe `.env.example`). O Spring lê via `spring.config.import: optional:file:.env[.properties]`.
- Arquivos de config devem estar em **LF** (já houve bug por CRLF no `.env`).
- Projeto roda dentro do WSL (Ubuntu 24.04).

## Convenções

- Commits em inglês, Conventional Commits, com a key do Jira: `feat: create listing endpoint (RH-13)`.
- Uma ideia por commit.
- Jira (projeto RH), Kanban: Backlog → A fazer → Em andamento → Em revisão → Concluído.

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

- RH-8 (setup) concluído: API sobe, conecta no Postgres, `/actuator/health` UP.
- RH-9 (estrutura de pacotes) e RH-10 (migration + entidade User) em andamento:
  `User` (record), `UserGateway`, `CreateUserUseCase` + `Impl` e `UserEntity` criados.
- Próximo: migration `V1__create_users_table.sql`, `UserGateway` como interface, adaptador JPA, mapper e registro do bean.