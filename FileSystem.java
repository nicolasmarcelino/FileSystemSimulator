import java.util.List;

public class FileSystem {

    private Diretorio root;

    public FileSystem() {
        this.root = new Diretorio("root");
    }

    public Arquivo criarArquivo(String nome, Diretorio pai) {
        if (pai == null) {
            throw new IllegalArgumentException("Diretório pai não pode ser nulo");
        }
        Arquivo arquivo = new Arquivo(nome, "");
        pai.adicionar(arquivo);
        return arquivo;
    }

    public Diretorio criarDiretorio(String nome, Diretorio pai) {
        if (pai == null) {
            throw new IllegalArgumentException("Diretório pai não pode ser nulo");
        }
        Diretorio novoDir = new Diretorio(nome);
        pai.adicionar(novoDir);
        return novoDir;
    }

    public Diretorio buscarDiretorioAqui(String nome, Diretorio pai) {
        if (pai == null) return null;
        for (FileType filho : pai.getFilhos()) {
            if (filho instanceof Diretorio && filho.getNome().equals(nome)) {
                return (Diretorio) filho;
            }
        }
        return null;
    }

    public List<FileType> listar(Diretorio dir) {
        if (dir == null) {
            throw new IllegalArgumentException("Diretório não pode ser nulo");
        }
        return dir.getFilhos();
    }

    public Diretorio getRoot() {
        return root;
    }
}