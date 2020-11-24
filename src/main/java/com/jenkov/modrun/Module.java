package com.jenkov.modrun;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author laiweiwei 2020-11-11
 */
public class Module {

    private String fullName;

    private String groupId;
    private String artifactId;
    private String version;

    private IClassStorage     classStorage;
    private ClassLoader       rootClassLoader;
    private ModuleClassLoader classLoader;

    private List<Module> dependencies;

    private Map<String, Class> loadedClasses = new ConcurrentHashMap<>();

    public Module(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        this.fullName = groupId.replace(".", "/") + "/" + artifactId.replace(".", "/") + "/" + version;
    }

    public String getFullName() {
        return fullName;
    }
    public String getGroupId() {
        return groupId;
    }
    public String getArtifactId() {
        return artifactId;
    }
    public String getVersion() {
        return version;
    }

    public ClassLoader getRootClassLoader() { return rootClassLoader;  }
    public void setRootClassLoader(ClassLoader rootClassLoader) { this.rootClassLoader = rootClassLoader; }

    public void setClassLoader(ModuleClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    public ModuleClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassStorage(IClassStorage classStorage) {
        this.classStorage = classStorage;
    }
    public IClassStorage getClassStorage() {
        return classStorage;
    }

    public List<Module> getDependencies() {
        return dependencies;
    }
    public void setDependencies(List<Module> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, Class> getLoadedClasses() {
        return loadedClasses;
    }

    public Class getClass(String className) throws IOException {
        Module ownerModule = findClass(className);

        if(ownerModule != null){
            return ownerModule.getClassFromThisModule(className);
        }

        //could be a core Java class - try the rootClassLoader.
        if(this.getRootClassLoader() != null){
            try {
                Class targetClass = this.getRootClassLoader().loadClass(className);
                if(targetClass != null){
                    return targetClass;
                }
            } catch (ClassNotFoundException e) {
                //ignore - we just return null instead.
            }
        }
        return null;
    }

    public List<URL> getResources(String resource) throws IOException {
        boolean ownerResource = getClassStorage().containsFile(resource);
        if(ownerResource){
            List<URL> files = getClassStorage().listFiles(resource);
            return files;
        }

        //could be a core Java class - try the rootClassLoader.
        if(this.getRootClassLoader() != null){
            Enumeration<URL> urls = this.getRootClassLoader().getResources(resource);
            if(urls != null){
                List<URL> list = new ArrayList<>();
                while (urls.hasMoreElements()) {
                    list.add(urls.nextElement());
                }
                return list;
            }
        }
        return null;
    }

    public URL getResource(String resource) throws IOException {
        boolean ownerResource = getClassStorage().containsFile(resource);

        if(ownerResource){
            return getClassStorage().toURL(resource);
        }

        //could be a core Java class - try the rootClassLoader.
        if(this.getRootClassLoader() != null){
            URL url = this.getRootClassLoader().getResource(resource);
            if(url != null){
                return url;
            }
        }
        return null;
    }

    public InputStream getResourceAsStream(String resource) throws IOException {
        boolean ownerResource = getClassStorage().containsFile(resource);
        if(ownerResource){
            byte[] bytes = getClassStorage().readFileBytes(resource);
            return new ByteArrayInputStream(bytes);
        }

        //could be a core Java class - try the rootClassLoader.
        if(this.getRootClassLoader() != null){
            InputStream stream = this.getRootClassLoader().getResourceAsStream(resource);
            if(stream != null){
                return stream;
            }
        }
        return null;
    }

    public Class getClassFromThisModule(String className) throws IOException {
        synchronized(this){
            Class theClass = null;
            theClass = this.loadedClasses.get(className);
            if(theClass != null){
                return theClass;
            }

            if(containsClass(className)){
                byte[] bytes = getClassStorage().readClassBytes(className);
                theClass = getClassLoader().defClass(className, bytes, 0, bytes.length);
                this.loadedClasses.put(className, theClass);
                return theClass;
            }
        }

        return null;
    }

    public Module findClass(String className) {
        if(containsClass(className)){
            return this;
        }
        return findClassInDependencies(className);
    }

    public Module findClassInDependencies(String className) {
        for (int i = 0; i < this.dependencies.size(); i++) {
            Module dependency = this.dependencies.get(i);
            if (dependency.containsClass(className)) {
                return dependency;
            }
        }

        for (int i = 0; i < this.dependencies.size(); i++) {
            Module dependency = this.dependencies.get(i);
            Module targetModule = dependency.findClassInDependencies(className);
            if (targetModule != null) {
                return targetModule;
            }
        }

        return null;
    }

    public boolean containsClass(String className) {
        if(this.classStorage == null){
            throw new NullPointerException("No IClassStorage implementation set on Module");
        }

        return this.classStorage.containsClass(className);
    }

}
