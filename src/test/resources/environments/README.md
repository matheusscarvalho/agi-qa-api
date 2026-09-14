# Ambientes de execução

Cada arquivo `<env>.properties` define as variáveis do ambiente. Selecione com
`-Denv` (`dev` | `qa` | `prod`); o padrão é `dev`.

```bash
mvn test -Denv=qa
mvn test -Denv=prod -Dgroups=smoke
```

Sobrepor pontualmente sem trocar de arquivo:

```bash
mvn test -DbaseUri=https://staging.dog.ceo
```

Precedência: `-DbaseUri` > arquivo do `-Denv` > padrão do código.
A Dog API é pública e única; ajuste as URLs caso haja ambientes internos.
