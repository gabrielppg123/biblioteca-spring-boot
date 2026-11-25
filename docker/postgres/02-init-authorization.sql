-- Executado somente em um volume PostgreSQL novo, no ambiente Docker local.
-- O usuario GERENTE inicial e o cliente Web usam senhas exclusivas de docker/.env.
\connect authorization_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS public.usuario (
    id UUID PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255),
    email VARCHAR(255),
    google_sub VARCHAR(255) UNIQUE,
    roles VARCHAR[] NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS public.client (
    id UUID PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL UNIQUE,
    client_secret VARCHAR(255) NOT NULL,
    redirect_uri VARCHAR(500),
    scope VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    tipo_cliente VARCHAR(20) NOT NULL
        CHECK (tipo_cliente IN ('INTERATIVO', 'INTEGRACAO'))
);

-- Le variaveis de ambiente do container sem gravar segredos no Git.
-- \getenv e suportado pelo psql 16 usado na imagem postgres:16.3.
\getenv senha_gerente BOOTSTRAP_ADMIN_PASSWORD
\getenv id_cliente BIBLIOTECA_WEB_CLIENT_ID
\getenv segredo_cliente BIBLIOTECA_WEB_CLIENT_SECRET

INSERT INTO public.usuario (id, login, senha, email, roles, ativo)
VALUES (
    gen_random_uuid(),
    'gerente-demo',
    crypt(:'senha_gerente', gen_salt('bf', 10)),
    'gerente-demo@example.invalid',
    ARRAY['GERENTE']::VARCHAR[],
    TRUE
);

INSERT INTO public.client
    (id, client_id, client_secret, redirect_uri, scope, ativo, tipo_cliente)
VALUES (
    gen_random_uuid(),
    :'id_cliente',
    crypt(:'segredo_cliente', gen_salt('bf', 10)),
    'http://localhost:8081/login/oauth2/code/biblioteca',
    'openid',
    TRUE,
    'INTERATIVO'
);
