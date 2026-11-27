import java.util.Scanner;
import java.util.List;
import java.io.IOException;

public class Shell {
    private static final String ARQUIVO_DADOS = "filesystem.dat";

    private static void initialApply(Journal journal, FileSystem fs) {
        List<JournalEntry> operations = journal.getOperationsForReapply();

        if (operations.isEmpty())
            return;

        for (JournalEntry op : operations) {
            fs.aplicar(op);
        }

        try {
            journal.cleanup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        FileSystem fs = FileSystem.carregarOuCriar(ARQUIVO_DADOS);
        Journal journal = new Journal();
        Diretorio atual = fs.getRoot();
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        initialApply(journal, fs);

        System.out.println("Bem-vindo ao sistema de arquivos! Digite 'help' para listar os comandos.");

        while (running) {
            System.out.print(atual.getCaminho() + "> ");
            String input = sc.nextLine().trim();

            if (input.isEmpty())
                continue;

            String[] parts = input.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "ls" -> {
                    List<FileType> itens = fs.listar(atual);
                    for (FileType f : itens) {
                        if (f instanceof Diretorio) {
                            System.out.println("[DIR]  " + f.getNome());
                        } else {
                            System.out.println("[ARQUIVO] " + f.getNome());
                        }
                    }
                }

                case "journal" -> {
                    List<String> log = journal.show();
                    if (log.isEmpty()) {
                        System.out.println("Journal Vazio!");
                        break;
                    }
                    log.forEach(System.out::println);
                }

                case "cd" -> {
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
                }

                case "mkdir" -> {
                    if (parts.length < 2) {
                        System.out.println("Uso: mkdir <nome do diretório>");
                        break;
                    }
                    int id = journal.begin("CREATE_DIR", new String[] { atual.getCaminho(), parts[1] });

                    try {
                        fs.criarDiretorio(parts[1], atual);
                        journal.commit(id);
                    } catch (Exception e) {
                        journal.abort(id);
                        System.out.println("\033[31m" + "Erro: " + e.getMessage() + "\033[0m");
                    }
                }

                case "touch" -> {
                    if (parts.length < 2) {
                        System.out.println("Uso: touch <nome do arquivo>");
                        break;
                    }
                    int id = journal.begin("CREATE_FILE", new String[] { atual.getCaminho(), parts[1] });

                    try {
                        fs.criarArquivo(parts[1], atual);
                        journal.commit(id);
                    } catch (Exception e) {
                        journal.abort(id);
                        System.out.println("\033[31m" + "Erro: " + e.getMessage() + "\033[0m");
                    }
                }

                case "rm" -> {
                    if (parts.length < 2) {
                        System.out.println("Uso: rm <nome do arquivo>");
                        break;
                    }
                    int id = journal.begin("DELETE_FILE", new String[] { atual.getCaminho(), parts[1] });

                    try {
                        fs.removerArquivo(parts[1], atual);
                        journal.commit(id);
                    } catch (Exception e) {
                        journal.abort(id);
                        System.out.println("\033[31m" + "Erro: " + e.getMessage() + "\033[0m");
                    }
                }

                case "rmdir" -> {
                    if (parts.length < 2) {
                        System.out.println("Uso: rmdir <nome do diretório>");
                        break;
                    }
                    int id = journal.begin("DELETE_DIR", new String[] { atual.getCaminho(), parts[1] });

                    try {
                        fs.removerDiretorio(parts[1], atual);
                        journal.commit(id);
                    } catch (Exception e) {
                        journal.abort(id);
                        System.out.println("\033[31m" + "Erro: " + e.getMessage() + "\033[0m");
                    }
                }

                case "mv" -> {
                    if (parts.length < 3) {
                        System.out.println("Uso: mv <path do arquivo> <path do arquivo> ");
                        break;
                    }

                    String pathAtual = resolverCaminho(parts[1], atual);
                    String pathNovo = resolverCaminho(parts[2], atual);
                    int id = journal.begin("MOVE", new String[] { pathAtual, pathNovo });

                    try {
                        fs.mover(pathAtual, pathNovo);
                        journal.commit(id);
                    } catch (Exception e) {
                        journal.abort(id);
                        System.out.println("\033[31m" + "Erro: " + e.getMessage() + "\033[0m");
                    }
                }

                case "help" -> {
                    System.out.println(
                            "Comandos: touch <nome do arquivo>, mkdir <nome do diretório>, cd <nome do diretório>, rm <nome do arquivo>, rmdir <nome do diretório> (OBS: não suporta path), mv <path do arquivo> <path do arquivo> (OBS: no momento apenas para alterar nome de diretorios e arquivos), ls, journal exit");
                }

                case "exit" -> {
                    try {
                        fs.salvar(ARQUIVO_DADOS);
                        journal.cleanup();
                    } catch (IOException e) {
                        System.out.println("Erro ao salvar: " + e.getMessage());
                    }
                    running = false;
                }

                default -> {
                    System.out.println("Comando desconhecido: " + cmd);
                }
            }
        }

        sc.close();
    }

    private static String resolverCaminho(String caminho, Diretorio atual) {
        if (caminho.startsWith("/")) {
            return caminho;
        }
        return atual.getCaminho() + "/" + caminho;
    }
}