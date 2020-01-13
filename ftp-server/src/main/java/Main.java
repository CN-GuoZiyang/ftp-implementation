import handler.FtpHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static final int CONTROL_PORT = 2211;
    private static ExecutorService executorPool;

    // FTP使用主动模式，控制端口2211，数据端口2200
    public static void main(String[] args) throws IOException {
        executorPool = Executors.newCachedThreadPool();
        ServerSocket server = new ServerSocket(CONTROL_PORT);
        while(true) {
            Socket clientSocket = server.accept();
            // 将连接放入新线程处理
            executorPool.submit(new FtpHandler(clientSocket));
        }
    }

}