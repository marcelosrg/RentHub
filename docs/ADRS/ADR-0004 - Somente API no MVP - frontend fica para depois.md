
**Status:** Aceito **Data:** 2026-10-13

## Contexto

O objetivo do projeto é demonstrar competência em backend Java/Spring Boot para vagas de nível pleno, com prazo até dezembro de 2026. Um frontend dobraria o escopo e consumiria tempo que não aparece numa entrevista de backend.

## Decisão

O MVP é uma API REST (`/api/v1`) documentada com OpenAPI/Swagger UI, que serve como interface de demonstração.

A API é desenhada para receber um frontend no futuro sem mudanças: CORS configurável, autenticação stateless com JWT e contratos estáveis.

## Alternativas consideradas

- **API + SPA (React/Next.js):** demonstração visual mais atraente, mas cerca do dobro do trabalho e muda o fluxo do OAuth (redirect no frontend).
- **Templates no servidor (Thymeleaf):** rápido de fazer, mas acopla a apresentação ao backend e não reflete o mercado atual.

## Consequências

- Todo o tempo vai para o que é avaliado: domínio, concorrência, testes, infraestrutura.
- Contratos de API pensados desde o início, sem atalhos para "facilitar o front".

- A demo é técnica (Swagger, Postman): menos impacto visual para quem não é dev.
- O fluxo completo do OAuth com redirect não é exercitado ponta a ponta até existir um cliente.