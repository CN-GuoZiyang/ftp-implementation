package utils;

import java.io.*;
import java.security.interfaces.RSAPrivateKey;

public class FileTransfer {

    public static void getFile(File file, InputStream inputStream, RSAPrivateKey privateKey) throws IOException {
        OutputStream outputStream = new FileOutputStream(file);
        while(true) {
            byte[] encryptedBytes = ReaderAndWriter.read(inputStream);
            if("finish".equals(new String(encryptedBytes))) {
                break;
            }
            byte[] bytes = RsaUtils.decryptWithPrivateKey(encryptedBytes, privateKey);
            byte[] after = new String(bytes).trim().getBytes();
            outputStream.write(after);
        }
        outputStream.close();
    }

}
