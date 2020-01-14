package handler;

import utils.FtpCommandResolver;
import utils.ReaderAndWriter;
import utils.RsaUtils;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class FtpHandler implements Runnable {

    private Socket socket;
    private String rootDir;
    private InputStream inputStream;
    private OutputStream outputStream;
    private RSAPrivateKey serverPrivateKey;
    private RSAPublicKey serverPublicKey;
    private RSAPublicKey clientPublicKey;

    public FtpHandler(Socket socket, String rootDir) {
        this.socket = socket;
        this.rootDir = rootDir;
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

        FtpCommandResolver resolver = new FtpCommandResolver(rootDir);
        while (true) {
            try {
                byte[] bytesEncrypted = ReaderAndWriter.read(inputStream);
                byte[] requestBytes = RsaUtils.decryptWithPrivateKey(bytesEncrypted, serverPrivateKey);
                String response = resolver.resolve(new String(requestBytes));
                byte[] responseBytes = RsaUtils.encryptWithPublicKey(response, clientPublicKey);
                ReaderAndWriter.write(responseBytes, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
