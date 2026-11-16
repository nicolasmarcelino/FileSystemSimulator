public class Arquivo extends FileType {

    private String content;

    public Arquivo(String nome, String content) {
        super(nome);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}