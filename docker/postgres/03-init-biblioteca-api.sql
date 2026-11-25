-- Executa o schema canonico da API no banco biblioteca_db.
-- O arquivo e montado no Compose fora de /docker-entrypoint-initdb.d
-- para impedir execucao acidental no authorization_db.
\connect biblioteca_db
\i /opt/biblioteca/schema-api.sql
