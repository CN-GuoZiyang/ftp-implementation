import utils.FileTransfer;
import utils.ReaderAndWriter;
import utils.RsaUtils;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Scanner;

public class Main {

    private static Socket socket;
    private static String serverIp;

    private static InputStream inputStream;
    private static OutputStream outputStream;

    private static RSAPublicKey serverPublicKey;
    private static RSAPublicKey clientPublicKey;
    private static RSAPrivateKey clientPrivateKey;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String connectLine = scanner.nextLine();
        if(connectLine.startsWith("ftp")) {
            String[] splits = connectLine.split(" ");
            serverIp = splits[1];
            int serverPort = Integer.parseInt(splits[2]);
            try {
                socket = new Socket(serverIp, serverPort);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            return;
        }

        try {
            byte[] publicKeyBytes = ReaderAndWriter.read(inputStream);
            serverPublicKey = RsaUtils.getPublicKeyFromBytes(publicKeyBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        KeyPair clientKeyPair = RsaUtils.genKeyPair();
        clientPublicKey = (RSAPublicKey) clientKeyPair.getPublic();
        clientPrivateKey = (RSAPrivateKey) clientKeyPair.getPrivate();

        try {
            ReaderAndWriter.write(clientPublicKey.getEncoded(), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            byte[] encryptBytes = ReaderAndWriter.read(inputStream);
            byte[] line = RsaUtils.decryptWithPrivateKey(encryptBytes, clientPrivateKey);
            assert line != null;
            System.out.println(new String(line));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            System.out.print("ftp > ");
            String command = scanner.nextLine();
            try {
                byte[] encryptedCommand = RsaUtils.encryptWithPublicKey(command, serverPublicKey);
                ReaderAndWriter.write(encryptedCommand, outputStream);
                if(command.startsWith("put")) {
                    String[] splits = command.split(" ");
                    File file = new File(splits[1]);
                    if(!file.exists()) {
                        System.out.println(splits[1] + " does not exists!");
                    } else if(file.isDirectory()) {
                        System.out.println(splits[1] + " is a directory!");
                    } else {
                        FileTransfer.putFile(file, inputStream, outputStream, serverPublicKey);
                    }
                } else if(command.startsWith("get")) {
                    handleGet(command);
                }
                byte[] responseEncrypted = ReaderAndWriter.read(inputStream);
                byte[] responseBytes = RsaUtils.decryptWithPrivateKey(responseEncrypted, clientPrivateKey);
                String response = new String(responseBytes);
                if(response.length() != 0) System.out.println(response);
                if("bye".equals(response)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void handleGet(String command) throws IOException {
        String[] splits = command.split(" ");
        String targetFileStr;
        if(splits.length == 2) {
            targetFileStr = splits[1].substring(splits[1].lastIndexOf("/") + 1);
        } else {
            if(splits[2].endsWith("/")) {
                targetFileStr = splits[2] + splits[1].substring(splits[1].lastIndexOf("/") + 1);
            } else {
                targetFileStr = splits[2];
                if(new File(targetFileStr).isDirectory()) {
                    targetFileStr = splits[2] + "/" + splits[1].substring(splits[1].lastIndexOf("/") + 1);
                }
            }
        }
        File targetFile = new File(targetFileStr);
        FileTransfer.getFile(targetFile, inputStream, clientPrivateKey);
    }

}
