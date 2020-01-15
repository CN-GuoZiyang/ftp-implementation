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

}
