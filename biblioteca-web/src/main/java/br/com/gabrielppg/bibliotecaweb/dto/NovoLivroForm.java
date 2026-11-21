package br.com.gabrielppg.bibliotecaweb.dto;

/** Formulario HTML; a API continua responsavel pela validacao definitiva. */
public class NovoLivroForm {
    private String isbn;
    private String titulo;
    private String dataPublicacao;
    private String genero;
    private String preco;
    private String idAutor;

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDataPublicacao() { return dataPublicacao; }
    public void setDataPublicacao(String dataPublicacao) { this.dataPublicacao = dataPublicacao; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getPreco() { return preco; }
    public void setPreco(String preco) { this.preco = preco; }
    public String getIdAutor() { return idAutor; }
    public void setIdAutor(String idAutor) { this.idAutor = idAutor; }
}
