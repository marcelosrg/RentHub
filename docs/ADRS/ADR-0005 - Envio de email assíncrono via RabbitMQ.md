

**Status:** Aceito (implementação na F5) **Data:** 2026-10-13

## Contexto

Quando uma reserva é confirmada ou cancelada, hóspede e anfitrião recebem email (RF25). O serviço de email é externo e pode estar lento ou fora do ar.

Se o envio acontecer dentro da requisição de reserva, uma falha no email faria a reserva falhar ou demorar, violando o RNF04 (falha no email não impede a reserva).

## Decisão

A reserva é gravada e confirmada na transação principal. Depois do commit, a aplicação publica um evento `BookingConfirmed` no RabbitMQ. Um consumidor (`@RabbitListener`, no mesmo monólito) recebe o evento e envia o email, com novas tentativas automáticas e fila de mensagens mortas (DLQ) para falhas persistentes.

## Alternativas consideradas

- **Envio síncrono na requisição:** simples, mas acopla a disponibilidade da reserva à do serviço de email.
- **`@Async` do Spring (thread separada, sem fila):** desacopla o tempo de resposta, mas se a aplicação reiniciar, os emails pendentes se perdem, e não há retentativa nem visibilidade.
- **Outbox pattern (evento gravado numa tabela na mesma transação, publicado depois):** garante que nenhum evento se perde mesmo se o RabbitMQ estiver fora, mas acrescenta uma tabela e um processo de publicação. Fica como evolução futura.

## Consequências

- A reserva responde rápido e não depende do email.
- Falhas temporárias são reprocessadas; falhas persistentes ficam na DLQ para análise.
- O tamanho da fila vira uma métrica observável (F5).

- Mais uma peça de infraestrutura para subir e monitorar.
- Consistência eventual: o email chega alguns instantes depois da confirmação.
- Sem outbox, existe uma janela pequena em que a reserva é gravada mas o evento não é publicado (se o RabbitMQ cair exatamente entre o commit e a publicação).