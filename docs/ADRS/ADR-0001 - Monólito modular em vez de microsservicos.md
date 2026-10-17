

**Status:** Aceito 
**Data:** 2026-10-13

## Contexto

O RentHub é desenvolvido por uma pessoa, com prazo de cerca de 11 semanas. Não há requisito de escala independente por módulo nem times diferentes cuidando de partes diferentes do sistema.

Microsserviços resolvem problemas de organização (vários times) e de escala (partes com carga muito diferente). O RentHub não tem nenhum dos dois hoje.

## Decisão

Uma única aplicação Spring Boot, com um único deploy, organizada em módulos de fronteira clara: `users`, `listings`, `bookings`, `search`, `notifications`.

Cada módulo expõe uma interface pública e não acessa diretamente as tabelas ou classes internas de outro.

## Alternativas consideradas

- **Microsserviços (um serviço por módulo):** escala e deploy independentes, mas exige comunicação em rede entre serviços, consistência distribuída (reserva + usuário em bancos separados), service discovery e observabilidade distribuída desde o primeiro dia. Custo alto demais para um dev e um prazo curto.
- **Monólito sem fronteiras (tudo misturado):** mais rápido no começo, mas vira um emaranhado difícil de testar e impossível de dividir depois.

## Consequências

- Um deploy, um banco, transações locais: a reserva é consistente sem saga nem 2PC.
- Debug e testes simples: tudo roda num processo só.
- As fronteiras entre módulos permitem extrair um serviço no futuro, se houver motivo real.

- Todos os módulos escalam juntos: se a busca precisar de 10 instâncias, a reserva vai junto.
- Um bug grave num módulo pode derrubar a aplicação inteira.
- Manter as fronteiras exige disciplina: nada no compilador impede um módulo de importar a classe interna de outro.