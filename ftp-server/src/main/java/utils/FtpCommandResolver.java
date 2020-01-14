package utils;

import java.io.File;

public class FtpCommandResolver {

    private String currentWorkingDirectory = "/";
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

            String targetDir = line.split(" ")[1];
            if(targetDir.startsWith("/")) {

            }

        } else if(line.startsWith("cd")) {

        } else if(line.equals("bye")) {

        }
        return "Known command!";
    }

}
