
## Contexto

O RentHub é um projeto de portfólio cujo objetivo é demonstrar domínio prático de Clean Architecture / Arquitetura Hexagonal, padrão frequente em vagas de nível pleno e em entrevistas técnicas.

Em Spring, a forma mais comum de "Clean Architecture" é parcial: entidades de domínio com `@Entity` e use cases anotados com `@Service`. Isso acopla o núcleo do sistema ao JPA e ao Spring, que é justamente o que o padrão quer evitar.

## Decisão

Aplicar a versão pura do padrão, com a regra de dependência apontando sempre para dentro:

- **`domain`**: Java puro. Entidades, value objects, regras de negócio e exceções de domínio. Nenhum import de Spring, Jakarta/JPA ou outra biblioteca de infraestrutura.
- **`application`**: use cases e **portas** (interfaces) de saída, como `UserRepository`. Também Java puro: sem `@Service`, sem `@Transactional`.
- **`infrastructure`**: adaptadores. Persistência com uma entidade JPA separada (`UserJpaEntity`), repositório Spring Data, adaptador que implementa a porta e mapper entre domínio e JPA. Aqui também ficam as classes `@Configuration` que **registram os use cases como beans**.
- **`web`**: controllers e DTOs de entrada e saída.

Organização por módulo primeiro (`user/`, `listing/`, `booking/`...), e cada módulo com essas quatro camadas (ADR-0001).

## Alternativas consideradas

- **Clean Architecture pragmática** (domínio com `@Entity`, use cases com `@Service`): menos classes e sem mappers, mas o domínio fica acoplado ao JPA e ao Spring. Para um CRUD em produção, é uma escolha razoável; para um portfólio cujo objetivo é demonstrar o padrão, enfraquece a demonstração.
- **Camadas técnicas tradicionais** (`controller/`, `service/`, `repository/` na raiz): o mais comum no mercado, mas não demonstra separação de domínio nem fronteiras entre módulos.

## Consequências

- O domínio e os use cases são testáveis com testes unitários puros, sem subir o Spring e sem banco.
- Trocar persistência, framework web ou integrações externas não afeta o núcleo.
- A estrutura demonstra o padrão de forma inequívoca para quem avalia o repositório.

- Mais código: para cada entidade persistida, uma classe de domínio, uma entidade JPA e um mapper.
- Os use cases não são detectados automaticamente pelo Spring; precisam ser registrados em classes `@Configuration`.
- Transações precisam ser tratadas na camada de infraestrutura (no adaptador ou num decorador do use case), já que a camada de aplicação não conhece `@Transactional`.
- Para CRUDs muito simples, o custo pode superar o benefício. Esse trade-off é consciente e justificado pelo objetivo do projeto.