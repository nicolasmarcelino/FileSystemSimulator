import java.time.LocalDateTime;

public class Journal {
    // TODO: fazer possivel JournalEntry, classe de dados para gravar no log separando classe Journal de operaçã
    private int id;
    private LocalDateTime timestamp;
    private String status; // TODO: possivel enum: COMMIT, BEGIN, ABORT

    // TODO: possivel enum: CREATE_DIR, CREATE_FILE, RENAME_FILE, RENAME_DIR, DELETE_DIR, DELETE_FILE...
    private String operation;
    private String[] params;



    public int begin(String opeation, String[] params){
        return 0;
    } // TODO: gravar linha BEGIN no log
    public void commit(int id){} // TODO: gravar o COMMIT do id
    public void abort(int id){} // TODO: para operações que falharam de forma normal(usuario errou)

    public void show(){} // TODO: imprimir o journal vira um comando do shell

    // TODO: definir o tipo de retorno
    // TODO: basicamente metodo para retornar operações de COMMIT para reaplicar
    public Journal getOperationForReapply(){
        return null;
    }

    // TODO: definir o tipo de retorno
    // TODO: metodo para retornar operações sem COMMIT
    public Journal getUnfinishedOperation(){
        return null;
    }

    // TODO: Marcador para indicar o salvamento no arquivo de dados
    public void checkpoint(){}
    // TODO: apagar valores anteriores ao checkpoint(limpeza dos logs)
    public void cleanup() {}

}
