# 📦 API de Assinaturas – Desafio Técnico

API REST para gerenciamento de usuários, planos e assinaturas com renovação automática, controle de falhas de pagamento e versionamento de preços.

Projeto desenvolvido em **Java 17 + Spring Boot 4** + Docker, seguindo boas práticas de arquitetura, concorrência, testabilidade e design próximo de ambiente produtivo.

---

## 🧠 Arquitetura / Qualidade (Critérios de Avaliação)

### Código bem estruturado
✅ SOLID  
✅ Camadas bem definidas (Controller / Service / Repository)  
✅ DTOs claros e imutáveis (records)  
✅ Exceptions centralizadas com `ProblemDetail`  

### Concorrência
✅ Lock de renovação de assinaturas vencidas  
✅ Índices parciais no banco (garantia de 1 assinatura ativa por usuário)  
✅ Atualização de preço idempotente  

### Desempenho
✅ Paginação em consultas  
✅ Processamento em batch no scheduler  
✅ Evita reprocessamento de assinaturas já tratadas  

### Testabilidade
✅ Controllers testáveis com `@WebMvcTest`  
✅ Serviços isoláveis com Mockito  
✅ Scheduler executável manualmente via endpoint admin  

### Criatividade / Extras
🔥 Histórico de preços por vigência  
🔥 Registro de tentativas de pagamento (`PaymentAttempt`)  
🔥 Endpoint administrativo para forçar renovação  
🔥 Logs claros e rastreáveis  
🔥 Design próximo de produção real  

---

## 🐳 Executar a aplicação com Docker

### Build das imagens
```bash
docker compose build
```

### Subir aplicação e banco
```bash
docker compose up
```

### Subir forçando rebuild
```bash
docker compose up --build
```

### Parar tudo e remover volumes (limpa banco)
```bash
docker compose down -v
```

### Remover também imagens
```bash
docker compose down -v --rmi all
```

⚠ Se precisar limpar a base, execute **down -v** e depois **up --build**
---

## 🧪 Executar os testes

### 1ª opção – Executar todos os testes
```bash
mvn test
```

### 2ª opção – Build + testes
```bash
mvn -U clean dependency:tree dependency:resolve dependency:resolve-plugins package
```

### 3ª opção – Executar testes pela IDE
- Importar o projeto como **Maven Project**
- Botão direito no projeto ou classe de teste
- **Run As → JUnit Test**

🔎 **Observações importantes**
- Testes de **controller** utilizam `@WebMvcTest`
- Nenhum teste de controller acessa banco real
- Dependências são mockadas com Mockito

---

## 🔄 Renovação automática de assinaturas

A renovação ocorre automaticamente no vencimento da assinatura via **scheduler**.

### Regras implementadas
- Renovação no dia do vencimento  
- Até **3 tentativas de pagamento**  
- Reagendamento automático em caso de falha  
- Suspensão da assinatura após exceder tentativas  

---

## 🛠 Forçar execução da renovação (Admin)

Endpoint administrativo criado para facilitar testes e operação:

```http
POST /admin/renewals/run
```

Esse endpoint:
- Executa o mesmo fluxo do scheduler
- Retorna métricas da execução (processadas, renovadas, falhas, suspensas)

---

## 📑 Planos e preços

Planos disponíveis:
- **BASIC** – R$ 19,90  
- **PREMIUM** – R$ 39,90  
- **FAMILY** – R$ 59,90  

### Diferenciais
- Preços possuem **vigência**
- Atualizações não sobrescrevem histórico
- Consultas sempre retornam o preço vigente

---

## 📘 Acessar documentação da API (Swagger)

Após subir a aplicação, a documentação pode ser acessada em:
```
http://localhost:8098/apiassinaturas/swagger-ui/index.html
```

---

## 🧩 Tecnologias utilizadas
- Java 17  
- Spring Boot 4  
- Spring Web MVC  
- Spring Data JPA  
- Flyway  
- PostgreSQL (runtime)  
- H2 (testes)  
- JUnit 5  
- Mockito  
- MockMvc  
- OpenAPI / Swagger  

---

## ✅ Conclusão

O projeto atende **integralmente** aos requisitos do desafio técnico, incluindo:
- Regras de negócio completas  
- Concorrência controlada  
- Testes automatizados modernos  
- Arquitetura limpa e extensível  

Com implementação de **boas práticas avançadas** e foco em qualidade de código, testabilidade e comportamento real de produção.
