# Guia de Contribuição - DoeAqui 🩸

Primeiro, obrigado por considerar contribuir para o **DoeAqui**! 
Somos um projeto Open Source focado em salvar vidas através da tecnologia, conectando doadores a quem precisa.

Para garantir a qualidade e a organização do nosso código, pedimos que siga as diretrizes abaixo.

## 🛠️ Tecnologias e Padrões

Antes de codificar, atente-se à nossa stack:
* **Java 25:** Utilizamos recursos modernos da linguagem.
* **Contract First:** **Importante!** Não altere os DTOs ou Controllers gerados manualmente. Toda alteração de API deve começar no arquivo OpenAPI (Swagger).
* **Persistência:** Usamos **JDBI** e **MySQL**. Evite JPA/Hibernate para manter o padrão.
* **MapStruct:** Usamos para conversão entre DTOs e Entidades de Domínio.

## 🚀 Como Contribuir

### 1. Reportando Bugs ou Sugerindo Funcionalidades
* Utilize a aba **Issues** do GitHub.
* Verifique se já não existe uma issue aberta sobre o mesmo tema.
* Seja detalhista: passos para reproduzir o erro ou descrição clara da nova funcionalidade.

### 2. Desenvolvimento (Pull Requests)

1.  **Fork o repositório** e crie uma branch para sua feature:
    * Padrão: `feature/nome-da-funcionalidade` ou `fix/correcao-bug`.
2.  **Ambiente Local:**
    * Certifique-se de rodar o `docker-compose up` para ter o MySQL e MongoDB ativos.
3.  **Contract First:**
    * Se for alterar a API, modifique primeiro o arquivo de especificação OpenAPI.
    * Rode o `mvn compile` para gerar as interfaces.
    * Implemente a lógica nas classes de serviço.
4.  **Migrations:**
    * Se alterar o banco de dados, crie um script SQL na pasta do **Flyway** (`V{versao}__descricao.sql`).

### 3. Padrões de Commit

Utilizamos o padrão **Conventional Commits**. Exemplo:
* `feat: adiciona busca por tipo sanguineo`
* `fix: corrige validação de CPF`
* `docs: atualiza documentação do endpoint`
* `style: formatação de código`

### 4. Testes
* O projeto usa **H2** para testes em memória.
* Certifique-se de que seus testes estão passando (`mvn test`) antes de abrir o PR.
* Novas funcionalidades devem vir acompanhadas de testes unitários.

## 🫱🏽‍🫲🏿 Código de Conduta

Este projeto é inclusivo e respeitoso. Não toleramos assédio ou discriminação de qualquer tipo. O objetivo é criar tecnologia para o bem social, e nossa comunidade deve refletir isso.

---
Dúvidas? Abra uma issue perguntando, ficaremos felizes em ajudar!