package com.jenkov.modrun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jjenkov on 24-10-2016.
 * @author laiweiwei on 2020-10-30
 */
public class ClassStorageDirectoryImpl implements IClassStorage {

    protected String classpath;

    public ClassStorageDirectoryImpl(String classpath) {
        this.classpath = classpath;
    }

    @Override
    public boolean exists() {
        return new File(this.classpath).exists();
    }

    @Override
    public boolean containsClass(String className){
        String classPath = toFullPath(className);
        return containsFile(classPath);
    }

    @Override
    public boolean containsFile(String filePath){
        Path path = Paths.get(filePath);
        File classFile = path.toFile();
        return classFile.exists();
    }


    @Override
    public byte[] readClassBytes(String className) throws IOException {
        String classPath = toFullPath(className);
        return readFileBytes(classPath);
    }

    @Override
    public byte[] readFileBytes(String filePath) throws IOException {
        Path pathToFile = Paths.get(filePath);
        File classFile = pathToFile.toFile();

        int fileLength = (int) classFile.length();

        byte[] classBytes = new byte[fileLength];

        try(FileInputStream input = new FileInputStream(classFile)){
            input.read(classBytes);
        }

        return classBytes;
    }

    private String toFullPath(String className) {
        String pathToClass     = className.replace(".", "/");
        String fullPathToClass = this.classpath + pathToClass + ".class";
        return fullPathToClass;
    }

}
