package com.jenkov.modrun;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Created by jjenkov on 24-10-2016.
 * @author laiweiwei on 2020-10-30
 */
public class ClassStorageDirectoryImpl implements IClassStorage {

    protected String classpath;
    protected File rawFile;

    public ClassStorageDirectoryImpl(String classpath) {
        this.classpath = classpath;
        this.rawFile = new File(this.classpath);
    }

    @Override
    public void close() {
        // ignore
    }

    @Override
    public File getRawFile() {
        return rawFile;
    }

    @Override
    public boolean exists() {
        return rawFile.exists();
    }

    @Override
    public boolean containsClass(String className){
        String classPath = toFullPath(className);
        return containsFile(classPath);
    }

    @Override
    public boolean containsFile(String filePath){
        Path path = Paths.get(classpath, filePath);
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
        Path pathToFile = Paths.get(classpath, filePath);
        File classFile = pathToFile.toFile();

        int fileLength = (int) classFile.length();

        byte[] classBytes = new byte[fileLength];

        try(FileInputStream input = new FileInputStream(classFile)){
            input.read(classBytes);
        }

        return classBytes;
    }

    @Override
    public URL toURL(String resource) throws MalformedURLException {
        Path pathToFile = Paths.get(classpath, resource);
        return new URL(pathToFile.toFile().getAbsolutePath());
    }

    @Override
    public List<URL> listFiles(String dir) throws IOException {
        Path pathToFile = Paths.get(classpath, dir);
        File[] list = pathToFile.toFile().listFiles();
        if (list == null) {
            return null;
        }
        List<URL> urls = new ArrayList<>();
        for (File f : list) {
            urls.add(new URL(f.getAbsolutePath()));
        }
        return urls;
    }

    @Override
    public List<URL> listClasses(String pkg) throws IOException {
        List<URL> files = listFiles(pkg.replace(".", "/"));
        return files.stream()
                .filter(f -> f.getPath().endsWith(".class"))
                .collect(Collectors.toList());
    }

    private String toFullPath(String className) {
        String pathToClass     = className.replace(".", "/");
        String fullPathToClass = this.classpath + pathToClass + ".class";
        return fullPathToClass;
    }

}
