package com.jenkov.modrun;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by jjenkov on 25-10-2016.
 * @author laiweiwei on 2020-10-30
 */
public class ClassStorageZipFileImpl implements IClassStorage {

    private String  zipFilePath;
    private ZipFile zipFile;

    public ClassStorageZipFileImpl(String zipFilePath) throws IOException {
        this.zipFilePath = zipFilePath;
        this.zipFile     = new ZipFile(this.zipFilePath);
    }

    @Override
    public boolean exists() {
        return true;
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

    @Override
    public byte[] readClassBytes(String className) throws IOException {
        String classFilePath = toClasspath(className);
        return readFileBytes(classFilePath);
    }

    @Override
    public byte[] readFileBytes(String filePath) throws IOException {
        ZipEntry zipEntry = this.zipFile.getEntry(filePath);
        byte[] fileBytes = new byte[(int) zipEntry.getSize()];

        InputStream inputStream = this.zipFile.getInputStream(zipEntry);
        inputStream.read(fileBytes);
        return fileBytes;
    }
}
