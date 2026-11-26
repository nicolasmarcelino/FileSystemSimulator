import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.util.Objects;


public class FileSystem {
    private byte[] mySecretKey = "secret".getBytes(StandardCharsets.UTF_8);
    private Diretorio root;

    public FileSystem() {
        this.root = new Diretorio("root");
    }

    public Arquivo criarArquivo(String nome, Diretorio pai) {
        if (pai == null) {
            throw new IllegalArgumentException("Diretório pai não pode ser nulo");
        }

        for(FileType ft : pai.getFilhos()){
            if(ft instanceof Diretorio) continue;

            if(Objects.equals(ft.nome, nome)){
                throw new IllegalArgumentException("Arquivo existente");
            }
        }

        Arquivo arquivo = new Arquivo(nome, "");
        pai.adicionar(arquivo);
        return arquivo;
    }

    public Diretorio criarDiretorio(String nome, Diretorio pai) {
        if (pai == null) {
            throw new IllegalArgumentException("Diretório pai não pode ser nulo");
        }

        for(FileType ft : pai.getFilhos()){
            if(ft instanceof Arquivo) continue;

            if(Objects.equals(ft.nome, nome)){
                throw new IllegalArgumentException("Diretório existente");
            }
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
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(root);
        }

        byte[] embaralhado = xor(buffer.toByteArray(), mySecretKey);

        try (FileOutputStream file = new FileOutputStream(caminhoArquivo)) {
            file.write(embaralhado);
        }
    }

    public void carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        byte[] embaralhado;
        try (FileInputStream file = new FileInputStream(caminhoArquivo)) {
            embaralhado = file.readAllBytes();
        }

        byte[] original = xor(embaralhado, mySecretKey);

        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(original))) {
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

    public void aplicar(JournalEntry op){
        Diretorio pai = this.buscarPorCaminho(op.params()[0]);
        System.out.println(op.params()[0]);
        if (pai == null) throw new IllegalStateException("caminho não existe: " + op.params()[0]);

        switch (op.operation()){
            case "CREATE_DIR"  -> criarDiretorio(op.params()[1], pai);
            case "CREATE_FILE" -> criarArquivo(op.params()[1], pai);
            default -> throw new IllegalArgumentException("operação desconhecida: " + op.operation());
        }
    }

    public Diretorio buscarPorCaminho(String caminho){
        String[] partes = caminho.split("/");
        if (partes.length < 2 || !partes[1].equals(root.getNome())) return root;

        Diretorio atual = root;
        for (int i = 2; i < partes.length; i++){
            atual = buscarDiretorioAqui(partes[i], atual);
            if(atual == null) return null;
        }

        return atual;
    }


    private byte[] xor(byte[] data, byte[] key) {
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++)
            out[i] = (byte) (data[i] ^ key[i % key.length]);
        return out;
    }
}