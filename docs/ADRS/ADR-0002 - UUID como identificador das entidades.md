

**Status:** Aceito 
**Data:** 2026-10-13

## Contexto

Os IDs aparecem na URL da API (`/api/v1/listings/{id}`). IDs sequenciais permitem adivinhar e varrer registros (`/listings/1`, `/2`, `/3`...) e revelam o volume do negócio (quantos anúncios e reservas existem).

## Decisão

Usar UUID como chave primária de todas as entidades, com o tipo nativo `uuid` do PostgreSQL.

## Alternativas consideradas

- **`BIGINT` sequencial:** 8 bytes, índice mais compacto e IDs legíveis, mas expõe a contagem e permite varredura.
- **`BIGINT` interno + UUID público (duas colunas):** o melhor dos dois mundos, mas dobra a complexidade de mapeamento para um ganho que este projeto não precisa.

## Consequências

- IDs não adivinháveis nem enumeráveis.
- O ID pode ser gerado na aplicação, antes do `INSERT`.

- 16 bytes por chave (contra 8): índices e FKs um pouco maiores.
- UUID v4 é aleatório, o que espalha os inserts pelo índice B-tree. No volume deste projeto, o impacto é desprezível; se virar problema, migrar para UUID v7 (ordenado por tempo).
- Menos legível em logs e debug.