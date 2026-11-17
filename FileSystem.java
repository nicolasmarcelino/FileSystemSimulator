import java.util.List;
import java.io.*;


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

    public void salvar(String caminhoArquivo) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(caminhoArquivo)))) {
            out.writeObject(root);
        }
    }

    public void carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(caminhoArquivo)))) {
            this.root = (Diretorio) in.readObject();
        }
    }

    public static FileSystem carregarOuCriar(String caminhoArquivo) {
        FileSystem fs = new FileSystem();
        File f = new File(caminhoArquivo);
        if (f.exists()) {
            try {
                fs.carregar(caminhoArquivo);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Falha ao carregar, iniciando vazio: " + e.getMessage());
            }
        }
        return fs;
    }
}