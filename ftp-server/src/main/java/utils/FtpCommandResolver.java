package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class FtpCommandResolver {

    private String actualDirectory;
    private String rootDirectory;
    private InputStream inputStream;
    private OutputStream outputStream;
    private RSAPublicKey clientPublicKey;
    private RSAPrivateKey serverPrivateKey;

    public FtpCommandResolver(String actualDirectory, InputStream inputStream, OutputStream outputStream, RSAPublicKey clientPublicKey, RSAPrivateKey serverPrivateKey) {
        this.actualDirectory = actualDirectory;
        this.rootDirectory = actualDirectory;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.clientPublicKey = clientPublicKey;
        this.serverPrivateKey = serverPrivateKey;
    }

    public String resolve(String line) throws IOException {
        if("ls".equals(line)) {

            File dir = new File(actualDirectory);
            File[] files = dir.listFiles();
            StringBuilder builder = new StringBuilder();
            for(File file : files) {
                builder.append(file.getName());
                builder.append("\t");
            }
            return builder.toString();

        } else if(line.startsWith("ls")) {

            String targetDirStr = line.split(" ")[1];
            File targetDir;
            if(targetDirStr.startsWith("/")) {
                targetDirStr = targetDirStr.substring(1);
                targetDir = new File(rootDirectory + targetDirStr);
            } else if(".".equals(targetDirStr)) {
                targetDir = new File(actualDirectory);
            } else if(targetDirStr.startsWith("./")) {
                targetDirStr = targetDirStr.substring(2);
                targetDir = new File(actualDirectory + targetDirStr);
            } else if("..".equals(targetDirStr)) {
                int lastSlash = actualDirectory.substring(0, actualDirectory.length()-1).lastIndexOf("/");
                targetDirStr = actualDirectory.substring(0, lastSlash+1);
                System.out.println(targetDirStr);
                if(targetDirStr.length() < rootDirectory.length()) {
                    targetDirStr = rootDirectory;
                }
                targetDir = new File(targetDirStr);
            } else if(targetDirStr.startsWith("../")) {
                targetDirStr = targetDirStr.substring(3);
                int lastSlash = actualDirectory.substring(0, actualDirectory.length()-1).lastIndexOf("/");
                targetDirStr = actualDirectory.substring(0, lastSlash+1) + targetDirStr;
                if(targetDirStr.length() < rootDirectory.length()) {
                    targetDirStr = rootDirectory;
                }
                targetDir = new File(targetDirStr);
            } else {
                targetDir = new File(actualDirectory + targetDirStr);
            }
            if(!targetDir.exists()) {
                return line.split(" ")[1] + " not exists!";
            }
            if(!targetDir.isDirectory()) {
                return line.split(" ")[1] + " is not a directory!";
            }
            File[] files = targetDir.listFiles();
            StringBuilder builder = new StringBuilder();
            for(File file : files) {
                builder.append(file.getName());
                builder.append("\t");
            }
            return builder.toString();

        } else if(line.startsWith("cd")) {

            String targetDirStr = line.split(" ")[1];
            File targetDir;
            if(".".equals(targetDirStr)) {
                return "";
            } else if(targetDirStr.startsWith("/")) {
                targetDirStr = rootDirectory + targetDirStr.substring(1);
            } else if(targetDirStr.startsWith("./")) {
                targetDirStr = actualDirectory + targetDirStr.substring(2);
            } else if("..".equals(targetDirStr)) {
                int lastSlash = actualDirectory.substring(0, actualDirectory.length()-1).lastIndexOf("/");
                targetDirStr = actualDirectory.substring(0, lastSlash+1);
                if(targetDirStr.length() < rootDirectory.length()) {
                    targetDirStr = rootDirectory;
                }
            } else if(targetDirStr.startsWith("../")) {
                targetDirStr = targetDirStr.substring(3);
                int lastSlash = actualDirectory.substring(0, actualDirectory.length()-1).lastIndexOf("/");
                targetDirStr = actualDirectory.substring(0, lastSlash+1) + targetDirStr;
            } else {
                targetDirStr = actualDirectory + targetDirStr;
            }
            targetDir = new File(targetDirStr);
            if(!targetDir.exists()) {
                return line.split(" ")[1] + " does not exist!";
            }
            if(!targetDir.isDirectory()) {
                return line.split(" ")[1] + " is not a directory!";
            }
            if(!targetDirStr.endsWith("/")) {
                targetDirStr += "/";
            }
            actualDirectory = targetDirStr;
            return "";

        } else if(line.equals("bye")) {
            return "bye";
        } else if(line.startsWith("put")) {

            String[] splits = line.split(" ");
            String targetFileStr;
            if(splits.length == 2) {
                if(splits[1].contains("/")) {
                    targetFileStr = splits[1].substring(splits[1].lastIndexOf("/") + 1);
                } else {
                    targetFileStr = splits[1];
                }
                targetFileStr = actualDirectory + targetFileStr;
            } else {
                targetFileStr = splits[2];
                if(targetFileStr.startsWith("/")) {
                    targetFileStr = rootDirectory + targetFileStr.substring(1);
                } else if(targetFileStr.startsWith("./")) {
                    targetFileStr = actualDirectory + targetFileStr.substring(2);
                } else if(targetFileStr.startsWith("../")) {
                    targetFileStr = targetFileStr.substring(3);
                    int lastSlash = actualDirectory.substring(0, actualDirectory.length()-1).lastIndexOf("/");
                    targetFileStr = actualDirectory.substring(0, lastSlash+1) + targetFileStr;
                } else {
                    targetFileStr = actualDirectory + targetFileStr;
                }
            }
            File targetFile = new File(targetFileStr);
            byte[] bytes = RsaUtils.encryptWithPublicKey("Ready", clientPublicKey);
            ReaderAndWriter.write(bytes, outputStream);
            FileTransfer.getFile(targetFile, inputStream, serverPrivateKey);
            return "";

        } else if(line.startsWith("get")) {

            String[] splits = line.split(" ");
            String targetFileStr = splits[1];
            if(targetFileStr.startsWith("/")) {
                targetFileStr = rootDirectory + targetFileStr.substring(1);
            } else if(targetFileStr.startsWith("./")) {
                targetFileStr = actualDirectory + targetFileStr.substring(2);
            } else if(targetFileStr.startsWith("../")) {
                targetFileStr = targetFileStr.substring(3);
                int lastSlash = actualDirectory.substring(0, actualDirectory.length()-1).lastIndexOf("/");
                targetFileStr = actualDirectory.substring(0, lastSlash+1) + targetFileStr;
            } else {
                targetFileStr = actualDirectory + targetFileStr;
            }
            File targetFile = new File(targetFileStr);
            if(!targetFile.exists()) {
                ReaderAndWriter.write(RsaUtils.encryptWithPublicKey(splits[1] + " does not exists!", clientPublicKey), outputStream);
                return "";
            } else if(targetFile.isDirectory()) {
                ReaderAndWriter.write(RsaUtils.encryptWithPublicKey(splits[1] + "  is a directory!", clientPublicKey), outputStream);
                return "";
            } else {
                ReaderAndWriter.write(RsaUtils.encryptWithPublicKey("Ready", clientPublicKey), outputStream);
                FileTransfer.putFile(targetFile, outputStream, clientPublicKey);
                return "";
            }

        }
        return "Unknown command!";
    }

}
