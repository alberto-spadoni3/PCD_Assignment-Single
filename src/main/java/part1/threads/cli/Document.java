package part1.threads.cli;

public class Document {
    private final String fileName;
    private final String content;

    public Document(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getName() {
        return this.fileName;
    }

    public String getContent() {
        return this.content;
    }
}
