package handler;

import utils.ReaderAndWriter;
import utils.RsaUtils;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class FtpHandler implements Runnable {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private RSAPrivateKey serverPrivateKey;
    private RSAPublicKey serverPublicKey;
    private RSAPublicKey clientPublicKey;

    public FtpHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        KeyPair serverKeyPair = RsaUtils.genKeyPair();
        serverPrivateKey = (RSAPrivateKey) serverKeyPair.getPrivate();
        serverPublicKey = (RSAPublicKey) serverKeyPair.getPublic();
        byte[] serverPublicKeyEncoded = serverPublicKey.getEncoded();
        try {
            ReaderAndWriter.write(serverPublicKeyEncoded, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            byte[] clientPublicKeyEncoded = ReaderAndWriter.read(inputStream);
            clientPublicKey = RsaUtils.getPublicKeyFromBytes(clientPublicKeyEncoded);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            byte[] msg = RsaUtils.encryptWithPublicKey("220 success", clientPublicKey);
            ReaderAndWriter.write(msg, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        FtpCommandResolver resolver = new FtpCommandResolver();
//        while (true) {
//            try {
//                String encryptedCommandLine = bufferedReader.readLine();
//                String commandLine = new String(RsaUtils.decryptWithPrivateKey(encryptedCommandLine, serverPrivateKey));
//                resolver.resolve(commandLine);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
