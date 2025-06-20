# Product Service

## Visão Geral
Microserviço responsável por operações CRUD de produtos.

## Estrutura do Projeto
- `src/main/java/com/insper/product`
  - `model/Product.java`: Entidade JPA.
  - `repository/ProductRepository.java`: Repositório Spring Data JPA.
  - `service/ProductService.java`: Lógica de negócio.
  - `controller/ProductController.java`: Endpoints REST.
  - `config/SecurityConfig.java`: Configuração de segurança (JWT).

## Como Rodar Localmente
1. Garanta que você tenha JDK 17 e Maven instalados.
2. Ajuste `application.properties` (no perfil `dev`) caso queira outro banco.
3. No diretório `product-service`, rode:
    ```
    mvn clean install
    mvn spring-boot:run
    ```
4. A API estará em `http://localhost:8081`.

### Endpoints
- `GET /products` → lista todos.
- `GET /products/{id}` → retorna produto por ID.
- `POST /products` (ROLE_ADMIN) → cria produto via JSON.
- `PUT /products/{id}` (ROLE_ADMIN) → atualiza produto.
- `DELETE /products/{id}` (ROLE_ADMIN) → deleta produto.

## Docker
Para gerar a imagem Docker:
