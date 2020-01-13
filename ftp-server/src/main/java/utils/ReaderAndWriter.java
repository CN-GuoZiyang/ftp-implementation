package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReaderAndWriter {

    public static byte[] read(InputStream inputStream) throws IOException {
        int in;
        StringBuilder builder = new StringBuilder();
        while((in = inputStream.read()) != 0) {
            if(in == 'a') {
                break;
            } else {
                char ch = (char) in;
                builder.append(ch);
            }
        }
        int length = Integer.valueOf(builder.toString());
        byte[] bytes = new byte[length];
        inputStream.read(bytes, 0, length);
        return bytes;
    }

    public static void write(byte[] bytes, OutputStream outputStream) throws IOException {
        outputStream.write(String.valueOf(bytes.length).getBytes());
        outputStream.write('a');
        outputStream.write(bytes);
        outputStream.flush();
    }

}
