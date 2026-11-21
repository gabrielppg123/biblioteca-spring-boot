-- FUTURO: somente quando separar fisicamente os bancos e após backup.
-- NÃO EXECUTAR AGORA: a aplicação antiga ainda usa a mesma base de dados.
-- O UUID id_usuario permanecerá gravado em autor e livro para auditoria.
ALTER TABLE public.autor DROP CONSTRAINT IF EXISTS fk_autor_usuario;
ALTER TABLE public.livro DROP CONSTRAINT IF EXISTS fk_livro_usuario;
