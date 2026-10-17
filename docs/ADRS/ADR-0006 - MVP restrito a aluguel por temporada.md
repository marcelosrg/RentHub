
**Status:** Aceito **Data:** 2026-10-13

## Contexto

A visão do RentHub é uma plataforma de imóveis com três modalidades: aluguel por temporada, aluguel de longa duração e venda.

As três têm fluxos de negócio diferentes. A temporada gira em torno de reserva por datas, com conflito e concorrência. Aluguel longo e venda giram em torno de visita, proposta, negociação e contrato. Implementar as três quase triplica o domínio, e o prazo do MVP é dezembro de 2026.

## Decisão

O MVP implementa somente o aluguel por temporada. Aluguel de longa duração e venda ficam registrados como evolução futura no README.

## Alternativas consideradas

- **Três modalidades completas:** a visão inteira, mas sem chance de terminar no prazo com qualidade.
- **Três modalidades, só a temporada com fluxo completo** (aluguel e venda apenas como anúncios pesquisáveis, com "tenho interesse"): mostra a visão maior, mas exige separar `Property` (imóvel) de `Listing` (oferta) agora e adiciona escopo antes do diferencial técnico estar pronto.

## Consequências

- Todo o esforço vai para o fluxo que demonstra concorrência, consistência e mensageria.
- O design atual (requisitos, ERD, sequência) continua válido sem mudanças.

- A plataforma parece menor do que a visão completa.
- Para adicionar as outras modalidades, o modelo precisará evoluir: separar `Property` (o imóvel físico) de `Listing` (uma oferta daquele imóvel, por temporada, aluguel ou venda). Essa migração é o primeiro passo da evolução e deve virar um novo ADR quando acontecer.