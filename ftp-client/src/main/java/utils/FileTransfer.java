package utils;

import java.io.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

public class FileTransfer {

    public static void putFile(File file, InputStream inputStream, OutputStream outputStream, RSAPublicKey publicKey) throws IOException {
        ReaderAndWriter.read(inputStream);
        InputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[64];
        while(fileInputStream.available() > 0) {
            int length = fileInputStream.read(buffer);
            byte[] encryptedBuffer = RsaUtils.encryptWithPublicKey(Arrays.copyOfRange(buffer, 0, length), publicKey);
            ReaderAndWriter.write(encryptedBuffer, outputStream);
        }
        ReaderAndWriter.write("finish".getBytes(), outputStream);
    }

    public static void getFile(File file, InputStream inputStream, RSAPrivateKey privateKey) throws IOException {
        byte[] readyBytes = RsaUtils.decryptWithPrivateKey(ReaderAndWriter.read(inputStream), privateKey);
        if(!"Ready".equals(new String(readyBytes))) {
            System.out.println(new String(readyBytes));
            return;
        }
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

}
