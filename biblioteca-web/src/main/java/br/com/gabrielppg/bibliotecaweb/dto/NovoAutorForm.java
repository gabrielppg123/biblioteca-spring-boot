package br.com.gabrielppg.bibliotecaweb.dto;

/** Valores do formulario HTML; datas sao validadas antes de chamar a API. */
public class NovoAutorForm {
    private String nome;
    private String dataNascimento;
    private String nacionalidade;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }
}
