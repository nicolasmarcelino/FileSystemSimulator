import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<JournalEntry> getOperationsForReapply(){
        List<JournalEntry> data = this.createOrLoad();

        Set<Integer> commited =  data.stream()
                .filter((it) -> Objects.equals(it.status(), "COMMIT"))
                .map(JournalEntry::id)
                .collect(Collectors.toSet());

        return  data.stream()
                .filter(
                        (it) ->
                                Objects.equals(it.status(), "BEGIN") && commited.contains(it.id())
                )
                .toList();
    }

    // TODO: definir o tipo de retorno
    // TODO: metodo para retornar operações sem COMMIT
    public JournalEntry getUnfinishedOperation(){
        return null;
    }

    public void cleanup() throws IOException {
        new FileOutputStream(DATA_LOG, false).close();
    }


    // PARA IMPRIMIR
    private String formattedTimestamp(LocalDateTime data){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return data.format(format);
    }

    private static final String COLUMN_GAP = "   ";

    public List<String> show(){
        List<JournalEntry> data = this.createOrLoad();
        if (data.isEmpty()) {
            return new ArrayList<>();
        }

        String[] header = {"ID", "DATA/HORA", "STATUS", "OPERAÇÃO", "PARÂMETROS"};
        List<String[]> rows = data.stream()
                .map(it -> new String[]{
                        String.valueOf(it.id()),
                        this.formattedTimestamp(it.timestamp()),
                        it.status(),
                        it.operation(),
                        String.join(" ", it.params())
                })
                .toList();

        int[] widths = new int[header.length];
        for (int i = 0; i < header.length; i++) {
            widths[i] = header[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        List<String> output = new ArrayList<>();
        output.add(formatRow(header, widths));
        output.add(separator(widths));
        for (String[] row : rows) {
            output.add(formatRow(row, widths));
        }
        return output;
    }

    private String formatRow(String[] columns, int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sb.append(COLUMN_GAP);
            sb.append(columns[i]);
            boolean lastColumn = i == columns.length - 1;
            if (!lastColumn) {
                sb.repeat(" ", widths[i] - columns[i].length());
            }
        }
        return sb.toString().stripTrailing();
    }

    private String separator(int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) sb.append(COLUMN_GAP);
            sb.repeat("─", widths[i]);
        }
        return sb.toString();
    }
}
