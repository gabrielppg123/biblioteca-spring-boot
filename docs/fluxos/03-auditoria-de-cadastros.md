# Fluxo 03: Auditoria por UUID (sem dependência da tabela usuário)

```mermaid
flowchart TD
    A["Web envia POST /autores com access token"] --> B["Biblioteca API valida JWT"]
    B --> C{"Tem authority GERENTE?"}
    C -->|Não| D["403 Forbidden"]
    C -->|Sim| E["Obtém usuario_id do JWT validado"]
    E --> F{"UUID válido?"}
    F -->|Não| D
    F -->|Sim| G["Valida os campos do autor"]
    G --> H["Salva autor.id_usuario = UUID"]
    H --> I["201 Created"]
```

O ID de usuário é gravado como valor UUID; não existe relacionamento JPA da API com `Usuario`.
Na fase transitória, o banco compartilhado ainda possui FKs antigas com `public.usuario`. Elas não são eliminadas automaticamente.
