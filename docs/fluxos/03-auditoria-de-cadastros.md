# Fluxo 03 — Auditoria por UUID, sem acoplamento com o banco de identidade

```mermaid
flowchart TD
    A["Web envia POST /autores com access token"] --> B["Biblioteca API valida JWT"]
    B --> C{"Authority GERENTE?"}
    C -->|Não| D["403 Forbidden"]
    C -->|Sim| E["Extrai usuario_id do JWT validado"]
    E --> F{"UUID válido?"}
    F -->|Não| D
    F -->|Sim| G["Valida campos do autor"]
    G --> H["Salva autor.id_usuario como UUID"]
    H --> I["201 Created"]
```

A API persiste `id_usuario` apenas como valor UUID em `biblioteca_db`. A identidade é administrada separadamente pelo Authorization Server em `authorization_db`; não existe relacionamento JPA da API com a entidade `Usuario`.
