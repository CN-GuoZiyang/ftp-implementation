package utils;

import java.io.*;
import java.security.interfaces.RSAPublicKey;

public class FileTransfer {

    public static void putFile(File file, InputStream inputStream, OutputStream outputStream, RSAPublicKey publicKey) throws IOException {
        ReaderAndWriter.read(inputStream);
        InputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[64];
        while(fileInputStream.available() > 0) {
            fileInputStream.read(buffer);
            byte[] encryptedBuffer = RsaUtils.encryptWithPublicKey(buffer, publicKey);
            ReaderAndWriter.write(encryptedBuffer, outputStream);
        }
        ReaderAndWriter.write("finish".getBytes(), outputStream);
    }

}
