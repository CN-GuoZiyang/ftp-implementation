import handler.FtpHandler;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static final int PORT = 2211;
    private static ExecutorService executorPool;

    // 端口2211
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(Main.class.getResource("properties.properties").getPath()));
        String rootDir = properties.getProperty("root_dir");
        if(!rootDir.endsWith("/")) {
            rootDir += "/";
        }
        executorPool = Executors.newCachedThreadPool();
        ServerSocket server = new ServerSocket(PORT);
        while(true) {
            Socket clientSocket = server.accept();
            // 将连接放入新线程处理
            executorPool.submit(new FtpHandler(clientSocket, rootDir));
        }
    }

}
