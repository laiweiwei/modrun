package com.jenkov.modrun;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by jjenkov on 23-10-2016.
 * @author laiweiwei on 2020-11-09
 */
public class ModuleClassLoader extends ClassLoader {

    private Module module;

    public ModuleClassLoader(Module module){
        this.module = module;
    }

    public String getName() {
        return getClass().getName();
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = this.module.getResources(name);
        return urls == null ? Collections.emptyEnumeration() : Collections.enumeration(urls);
    }

    @Override
    public InputStream getResourceAsStream(String resource) {
        try {
            return this.module.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resource, e);
        }
    }

    @Override
    public URL getResource(String resource) {
        try {
            return this.module.getResource(resource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resource, e);
        }
    }

    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        try {
            Class theClass = this.module.getClass(className);
            if(theClass == null){
                throw new ClassNotFoundException("Could not find class: " + className);
            }
            return theClass;
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to load class: " + className, e);
        }
    }

    public Class defClass(String className, byte[] classBytes, int offset, int length){
        return defineClass(className, classBytes, offset, length);
    }


}
