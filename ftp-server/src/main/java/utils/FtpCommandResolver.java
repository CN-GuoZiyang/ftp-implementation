package utils;

import java.io.File;

public class FtpCommandResolver {

    private String actualDirectory;
    private String rootDirectory;

    public  FtpCommandResolver(String actualDirectory) {
        this.actualDirectory = actualDirectory;
        this.rootDirectory = actualDirectory;
    }

    public String resolve(String line) {
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
                return line.split(" ")[1] + " not exists!";
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
        }
        return "Known command!";
    }

}
