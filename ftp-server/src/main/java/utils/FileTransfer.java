package utils;

import java.io.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

public class FileTransfer {

    public static void getFile(File file, InputStream inputStream, RSAPrivateKey privateKey) throws IOException {
        OutputStream outputStream = new FileOutputStream(file);
        while(true) {
            byte[] encryptedBytes = ReaderAndWriter.read(inputStream);
            if("finish".equals(new String(encryptedBytes))) {
                break;
            }
            byte[] bytes = RsaUtils.decryptWithPrivateKey(encryptedBytes, privateKey);
            outputStream.write(bytes);
        }
        outputStream.close();
    }

    public static void putFile(File file, OutputStream outputStream, RSAPublicKey publicKey) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[64];
        while(inputStream.available() > 0) {
            int length = inputStream.read(buffer);
            byte[] encryptedBuffer = RsaUtils.encryptWithPublicKey(Arrays.copyOfRange(buffer, 0, length), publicKey);
            ReaderAndWriter.write(encryptedBuffer, outputStream);
        }
        ReaderAndWriter.write("finish".getBytes(), outputStream);
    }

}
