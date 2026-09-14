# QA API — Dog API

Automação de testes da [Dog API](https://dog.ceo/dog-api/documentation) com
**RestAssured + JUnit 5 (Java/Maven)**, cobrindo os endpoints pedidos no desafio.

## Endpoints cobertos

| Endpoint | Cenários |
|----------|----------|
| `GET /breeds/list/all` | contrato (200, JSON, schema, tipos); raças/sub-raças conhecidas |
| `GET /breed/{breed}/images` | contrato de **envio** + de **resposta** (data-driven); raça inexistente → 404; case-insensitive |
| `GET /breeds/image/random` | contrato de resposta; variação entre chamadas |
| `GET /breed/{breed}/list` | sub-raças (fluxo alternativo); raça sem sub-raças = lista vazia |
| `GET /breed/{breed}/{sub}/images` | contrato de envio + resposta (sub-raça) |
| `GET /breed/{breed}/images/random` e `/random/{n}` | 1 ou N imagens (fluxos alternativos) |
| Exceções | raças inválidas → 404; sub-raça inexistente → 404; `POST` em GET → 405; rota inexistente → 404 |

### Validações de contrato

- **Contrato da resposta:** status code, `Content-Type`, **JSON Schema** por
  endpoint (com `additionalProperties:false`, `required`, `enum`, `pattern` e
  `format`) e **desserialização em modelos tipados** (POJOs) — que falha se a
  resposta trouxer campo extra ou tipo divergente.
- **Contrato de envio:** como a Dog API é **GET (sem corpo)**, o contrato dos
  dados enviados é validado sobre os **parâmetros de caminho** (`breed`,
  `subBreed`) via JSON Schema, antes da chamada (`ContractValidator`). O mesmo
  mecanismo valida o **body** de um POST/PUT quando houver — inclusive há caso
  negativo garantindo que entradas fora do padrão são rejeitadas.

## Pré-requisitos
- JDK 17+
- Maven 3.9+
- Acesso à internet (a API é pública)

## Execução

```bash
mvn test
```

### Execução por tag (grupos JUnit)

| Grupo | Escopo | Comando |
|-------|--------|---------|
| `smoke` | contrato dos endpoints principais | `mvn test -Dgroups=smoke` |
| `regression` | suíte completa | `mvn test -Dgroups=regression` |
| `contract` | contrato de envio/resposta | `mvn test -Dgroups=contract` |
| `exception` | 404/405/rota inexistente | `mvn test -Dgroups=exception` |
| `alternative` | sub-raças, random por raça, N imagens | `mvn test -Dgroups=alternative` |

Excluir um grupo: `mvn test -DexcludedGroups=exception`.

### Ambientes de execução (dev / qa / prod)

As configurações por ambiente ficam em
[`src/test/resources/environments/`](src/test/resources/environments/), um
arquivo `.properties` por ambiente. Selecione com `-Denv` (padrão: `dev`):

```bash
mvn test -Denv=qa
mvn test -Denv=prod -Dgroups=smoke
```

Sobrepor pontualmente sem trocar de arquivo:

```bash
mvn test -DbaseUri=https://staging.dog.ceo
```

Precedência: `-DbaseUri` > arquivo do `-Denv` > padrão do código.

## Qualidade (formatação)

```bash
mvn spotless:check     # verifica a formatação (imports, espaços, newline)
mvn spotless:apply     # aplica a formatação
```

O `spotless:check` também roda no CI antes dos testes.

## Relatório de resultados

```bash
mvn surefire-report:report-only -DshowSuccess=true
```

- **HTML:** `target/reports/surefire.html`
- **XML (CI):** `target/surefire-reports/*.xml`

Em caso de falha, a request/response completa é logada automaticamente
(`enableLoggingOfRequestAndResponseIfValidationFails`).

## Estrutura

```
agi-qa-api/
├── pom.xml
├── src/test/java/com/agi/dogapi/
│   ├── clients/DogApiClient.java     # chamadas HTTP encapsuladas
│   ├── support/BaseApiTest.java      # RequestSpecification compartilhada
│   ├── models/                       # POJOs para contrato tipado da resposta
│   ├── support/
│   │   ├── BaseApiTest.java          # RequestSpecification compartilhada
│   │   └── ContractValidator.java    # contrato dos dados de envio (JSON Schema)
│   └── tests/
│       ├── ListAllBreedsTest.java
│       ├── BreedImagesTest.java
│       ├── RandomImageTest.java
│       ├── AlternativeFlowsTest.java # sub-raças, random por raça, N imagens
│       └── ExceptionFlowsTest.java   # 404, 405, rota inexistente
├── src/test/resources/schemas/       # JSON Schemas (respostas + request/)
├── postman/                          # coleção Postman/Newman (bônus)
└── .github/workflows/api.yml
```

## Decisões de arquitetura
- **Client isolado** (`DogApiClient`): os testes não montam requisições; se um
  endpoint mudar, só o client muda.
- **`RequestSpecification` única** em `BaseApiTest` (base URI, path, headers, logs).
- **JSON Schema** por endpoint: garante o contrato/estrutura, não só valores.
- **Testes parametrizados** para múltiplas raças, evitando duplicação.

## Bônus — Postman/Newman

Uma coleção equivalente está em [`postman/`](postman/) para execução manual ou
via Newman:

```bash
npx newman run postman/DogAPI.postman_collection.json
```

## Última execução local

```
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
