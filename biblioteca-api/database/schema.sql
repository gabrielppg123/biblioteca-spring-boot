-- Estrutura para um banco da Biblioteca API independente (fase futura).
-- NUNCA executar sobre um banco existente sem revisar/backups.
-- id_usuario é UUID de auditoria; não há chave estrangeira para usuario de outro serviço.
CREATE TABLE IF NOT EXISTS public.autor (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    nacionalidade VARCHAR(50) NOT NULL,
    data_cadastro TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    id_usuario UUID NOT NULL,
    CONSTRAINT uk_autor_dados UNIQUE (nome, data_nascimento, nacionalidade)
);
CREATE TABLE IF NOT EXISTS public.livro (
    id UUID PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    titulo VARCHAR(150) NOT NULL,
    data_publicacao DATE,
    genero VARCHAR(30) NOT NULL,
    preco NUMERIC(18,2),
    data_cadastro TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    id_autor UUID NOT NULL REFERENCES public.autor(id),
    id_usuario UUID NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_livro_autor ON public.livro(id_autor);
CREATE INDEX IF NOT EXISTS idx_autor_usuario ON public.autor(id_usuario);
CREATE INDEX IF NOT EXISTS idx_livro_usuario ON public.livro(id_usuario);
