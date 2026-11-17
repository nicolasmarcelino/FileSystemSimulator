import java.util.Scanner;
import java.util.List;
import java.io.IOException;

public class Shell {
    private static final String ARQUIVO_DADOS = "filesystem.dat";

    public static void main(String[] args) {
        
        FileSystem fs = FileSystem.carregarOuCriar(ARQUIVO_DADOS);
        Diretorio atual = fs.getRoot();
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        System.out.println("Bem-vindo ao sistema de arquivos! Digite 'help' para listar os comandos.");

        while (running) {
            System.out.print(atual.getCaminho() + "> ");
            String input = sc.nextLine().trim();

            if (input.isEmpty())
                continue;

            String[] parts = input.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "ls":
                    List<FileType> itens = fs.listar(atual);
                    for (FileType f : itens) {
                        if (f instanceof Diretorio) {
                            System.out.println("[DIR]  " + f.getNome());
                        } else {
                            System.out.println("[ARQUIVO] " + f.getNome());
                        }
                    }
                    break;

                case "cd":
                    if (parts.length < 2) {
                        System.out.println("Uso: cd <nome do diretório>");
                        break;
                    }
                    if (parts[1].equals("..")) {
                        if (atual.getPai() != null) {
                            atual = atual.getPai();
                        }
                        break;
                    }

                    Diretorio dir = fs.buscarDiretorioAqui(parts[1], atual);
                    if (dir != null) {
                        atual = dir;
                    } else {
                        System.out.println("O diretório não existe.");
                    }
                    break;

                case "mkdir":
                    if (parts.length < 2) {
                        System.out.println("Uso: mkdir <nome do diretório>");
                        break;
                    }
                    fs.criarDiretorio(parts[1], atual);
                    break;

                case "touch":
                    if (parts.length < 2) {
                        System.out.println("Uso: touch <nome do arquivo>");
                        break;
                    }
                    fs.criarArquivo(parts[1], atual);
                    break;

                case "help":
                    System.out.println("Comandos: touch <nome do arquivo>, mkdir <nome do diretório>, cd <nome do diretório>, ls, exit");
                    break;

                case "exit":
                    try {
                        fs.salvar(ARQUIVO_DADOS);
                    } catch (IOException e) {
                        System.out.println("Erro ao salvar: " + e.getMessage());
                    }
                    running = false;
                    break;

                default:
                    System.out.println("Comando desconhecido: " + cmd);
            }
        }

        sc.close();
    }
}