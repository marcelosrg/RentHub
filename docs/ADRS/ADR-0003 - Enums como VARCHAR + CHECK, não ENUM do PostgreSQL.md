

**Status:** Aceito **Data:** 2026-10-13

## Contexto

Várias colunas têm um conjunto fechado de valores: `listings.type`, `listings.status`, `bookings.status`, `users.auth_provider`. Esses conjuntos vão mudar com o tempo (ex.: `PENDING` em `bookings.status` se um dia entrar pagamento).

## Decisão

Armazenar como `VARCHAR`, com uma constraint `CHECK` listando os valores válidos. No Java, mapear com `@Enumerated(EnumType.STRING)`.

```sql
status VARCHAR(20) NOT NULL
  CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'))
```

## Alternativas consideradas

- **`ENUM` nativo do PostgreSQL:** ocupa menos espaço e é validado pelo tipo, mas adicionar um valor exige `ALTER TYPE`, remover um valor não é suportado diretamente, e o mapeamento com JPA/Hibernate precisa de configuração extra.
- **`@Enumerated(EnumType.ORDINAL)` (salvar como número):** compacto, mas reordenar ou inserir um valor no meio do enum Java corrompe silenciosamente todos os dados existentes. Descartado.
- **Tabela de domínio (lookup table) com FK:** flexível e editável sem deploy, mas exagerado para conjuntos pequenos que só mudam junto com o código.

## Consequências

- Adicionar um valor = uma migration trocando o `CHECK`, sem mexer no tipo da coluna.
- Os dados ficam legíveis direto no banco (`'ACTIVE'`, não `2`).
- Mapeamento JPA trivial.

- Alguns bytes a mais por linha do que um `ENUM` nativo.
- O `CHECK` e o enum Java precisam ser mantidos em sincronia manualmente.