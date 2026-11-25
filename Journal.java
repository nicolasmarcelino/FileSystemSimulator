import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Journal {
    private int nextId;
    private final String DATA_LOG = "journal.log";

    public Journal(){
        List<JournalEntry> data = this.createOrLoad();
        nextId = data.stream().mapToInt(JournalEntry::id).max().orElse(0) + 1;
    }


    private List<JournalEntry> load() throws IOException, ClassNotFoundException {
        List<JournalEntry> entries = new ArrayList<>();
        try(BufferedReader reader = Files.newBufferedReader(Path.of((DATA_LOG)))){
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null){
                count++;
                if(line.isBlank()) continue;
                try {
                    entries.add(parse(line));
                }catch (RuntimeException e){
                    System.out.println("journal: linha " + count + " ignorada (corrompida)");
                }
            }
        }

        return entries;
    }

    private JournalEntry parse(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 4) throw new IllegalArgumentException("campos insuficientes");
        return new JournalEntry(
                Integer.parseInt(fields[0]),
                LocalDateTime.parse(fields[1]),
                fields[2],
                fields[3],
                Arrays.copyOfRange(fields, 4, fields.length));
    }

    private void append(JournalEntry e) throws IOException {
        String line = e.id() + "|" + e.timestamp() + "|" + e.status() + "|" + e.operation()
                + (e.params().length > 0 ? "|" + String.join("|", e.params()) : "");

        try (FileOutputStream out = new FileOutputStream(DATA_LOG, true)) {
            out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }
    }

    private List<JournalEntry> createOrLoad() {
        List<JournalEntry> entryList = new ArrayList<JournalEntry>();
        File f = new File(DATA_LOG);
        if (f.exists()) {
            try {
                entryList = this.load();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Falha ao carregar, iniciando vazio: " + e.getMessage());
            }
        }

        return entryList;
    }


    public int begin(String operation, String[] params) {
        int id = nextId++;
        String status = "BEGIN";
        JournalEntry entry = new JournalEntry(id, LocalDateTime.now(), status, operation, params);

        try {
            this.append(entry);
        } catch (IOException e) {
            throw new RuntimeException("erro ao gravar BEGIN no journal");
        }

        return id;
    }

    public void commit(int id){
        String status = "COMMIT";
        JournalEntry entry = new JournalEntry(id, LocalDateTime.now(), status, "", new String[]{});

        try {
            this.append(entry);
        } catch (IOException e) {
            throw new RuntimeException("erro ao gravar COMMIT no journal");
        }
    }
    public void abort(int id){
        String status = "ABORT";
        JournalEntry entry = new JournalEntry(id, LocalDateTime.now(), status, "", new String[]{});

        try {
            this.append(entry);
        } catch (IOException e) {
            throw new RuntimeException("erro ao gravar ABORT no journal");
        }
    }

    public void show(){} // TODO: imprimir o journal vira um comando do shell

    // TODO: definir o tipo de retorno
    // TODO: basicamente metodo para retornar operações de COMMIT para reaplicar
    public JournalEntry getOperationForReapply(){
        return null;
    }

    // TODO: definir o tipo de retorno
    // TODO: metodo para retornar operações sem COMMIT
    public JournalEntry getUnfinishedOperation(){
        return null;
    }

    // TODO: Marcador para indicar o salvamento no arquivo de dados
    public void checkpoint(){}
    // TODO: apagar valores anteriores ao checkpoint(limpeza dos logs)
    public void cleanup() {}
}
