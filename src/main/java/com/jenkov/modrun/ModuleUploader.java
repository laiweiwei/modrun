package com.jenkov.modrun;

import java.io.*;

/**
 * Created by laiweiwei on 2020-11-10.
 */
public class ModuleUploader {

    private InputStream jarUploaded;
    private String localRepositoryBaseFilePath;

    public ModuleUploader(InputStream jarUploaded, String localRepositoryBaseFilePath) {
        this.jarUploaded = jarUploaded;
        this.localRepositoryBaseFilePath = localRepositoryBaseFilePath;
        if(!this.localRepositoryBaseFilePath.endsWith("/")){
            this.localRepositoryBaseFilePath += "/";
        }
    }

    public File upload(String groupId, String artifactId, String artifactVersion) throws IOException {
        File jarFileDirectory = new File(ModulePath.getModuleDirectoryPath(this.localRepositoryBaseFilePath, groupId, artifactId, artifactVersion));
        if(!jarFileDirectory.exists()){
            jarFileDirectory.mkdirs();
        }

        String jarPath = ModulePath.getModuleJarFilePath(this.localRepositoryBaseFilePath, groupId, artifactId, artifactVersion);
        File jarFile = new File(jarPath);
        try(FileOutputStream jarFileOutput = new FileOutputStream(jarFile); InputStream input = this.jarUploaded) {
            int data = input.read();
            while(data != -1){
                jarFileOutput.write(data);
                data = input.read();
            }
        }
        return jarFile;
    }

}
