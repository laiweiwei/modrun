package com.jenkov.modrun;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by jjenkov on 25-10-2016.
 * @author laiweiwei on 2020-10-30
 */
public class ClassStorageZipFileImpl implements IClassStorage {

    private String  zipFilePath;
    private File rawFile;
    private ZipFile zipFile;

    public ClassStorageZipFileImpl(String zipFilePath) throws IOException {
        this.zipFilePath = zipFilePath;
        this.rawFile = new File(zipFilePath);
        this.zipFile = new ZipFile(this.rawFile);
    }

    @Override
    public boolean exists() {
        return this.rawFile.exists();
    }

    @Override
    public boolean containsClass(String className) {
        String classFilePath = toClasspath(className);
        return containsFile(classFilePath);
    }

    @Override
    public boolean containsFile(String filePath) {
        return this.zipFile.getEntry(filePath) != null;
    }

    private String toClasspath(String className) {
        return className.replace(".", "/") + ".class";
    }

    private String toFilePath(String className) {
        return className.replace(".", "/");
    }

    @Override
    public byte[] readClassBytes(String className) throws IOException {
        String classFilePath = toClasspath(className);
        return readFileBytes(classFilePath);
    }

    @Override
    public byte[] readFileBytes(String filePath) throws IOException {
        ZipEntry zipEntry = this.zipFile.getEntry(filePath);
        if (zipEntry == null) {
            return null;
        }
        byte[] fileBytes = new byte[(int) zipEntry.getSize()];

        InputStream inputStream = this.zipFile.getInputStream(zipEntry);
        inputStream.read(fileBytes);
        return fileBytes;
    }

    @Override
    public URL toURL(String resource) throws MalformedURLException {
        URL url = new URL("jar:file:"+zipFile.getName()+"!/"+resource);
        return url;
    }

    @Override
    public List<URL> listFiles(String dir) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        if (entries == null || !entries.hasMoreElements()) {
            return null;
        }
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        List<URL> urls = new ArrayList<>();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String name = zipEntry.getName();
            if (!name.startsWith(dir)) {
                continue;
            }
            urls.add(toURL(name));
        }
        return urls;
    }

    @Override
    public List<URL> listClasses(String pkg) throws IOException {
        List<URL> files = listFiles(toFilePath(pkg));
        return files.stream()
            .filter(f -> f.getPath().endsWith(".class"))
            .collect(Collectors.toList());
    }

}
