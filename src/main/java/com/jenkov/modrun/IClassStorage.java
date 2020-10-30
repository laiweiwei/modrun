package com.jenkov.modrun;

import java.io.IOException;

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

}
