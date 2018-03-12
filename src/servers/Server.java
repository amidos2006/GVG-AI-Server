package servers;

public abstract class Server {
    public abstract String extract(String filename) throws Exception;
    public abstract void compile(String filename) throws Exception;
    public abstract void run(String fileName, String packageName) throws Exception;
}
