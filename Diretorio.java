import java.util.ArrayList;
import java.util.List;

public class Diretorio extends FileType {

    private List<FileType> filhos;

    public Diretorio(String nome) {
        super(nome);
        this.filhos = new ArrayList<>();
    }

    public void adicionar(FileType file) {
        file.pai = this;
        filhos.add(file);
    }

    public boolean remover(String nome) {
        return filhos.removeIf(f -> f.getNome().equals(nome));
    }

    public FileType buscar(String nome) {
        for (FileType f : filhos) {
            if (f.getNome().equals(nome)) {
                return f;
            }
        }
        return null;
    }

    public List<FileType> getFilhos() {
        return filhos;
    }

    @Override
    public String toString() {
        return "Diretorio{" + super.toString() +
                ", filhos=" + filhos.size() + "}";
    }
}