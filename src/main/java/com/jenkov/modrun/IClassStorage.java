package com.jenkov.modrun;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by jjenkov on 24-10-2016.
 * @author laiweiwei on 2020-10-30
 */
public interface IClassStorage {

    public boolean exists();

    public boolean containsClass(String className);

    public boolean containsFile(String filePath);

    public byte[] readClassBytes(String className) throws IOException;

    public byte[] readFileBytes(String filePath) throws IOException;

    public URL toURL(String resource) throws MalformedURLException;

    public List<URL> listFiles(String dir) throws IOException;

    public List<URL> listClasses(String pkg) throws IOException;

}
