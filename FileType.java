import java.time.LocalDateTime;
import java.io.Serializable;

public abstract class FileType implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String nome;
    protected LocalDateTime dataCriacao;
    protected Diretorio pai;

    public FileType(String nome) {
        this.nome = nome;
        this.dataCriacao = LocalDateTime.now();
    }

    public Diretorio getPai() {
        return pai;
    }

    public String getNome() {
        return nome;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public String getCaminho() {
        if (getPai() == null)
            return "/" + getNome();
        return getPai().getCaminho() + "/" + getNome();
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "nome='" + nome + "', dataCriacao=" + dataCriacao;
    }
}