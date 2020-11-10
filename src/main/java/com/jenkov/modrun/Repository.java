package com.jenkov.modrun;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by jjenkov on 23-10-2016.
 * @author laiweiwei on 2020-10-30
 */
public class Repository {

    public final static List<String> NOT_RUNTIME_SCOPE = Arrays.asList("test", "system", "provided");
    private String rootDir;

    public Repository(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String createPathToModuleJar(Module module){
        return this.rootDir + "/" + module.getFullName() + "/" + module.getArtifactId() + "-" + module.getVersion() + ".jar";
    }

    public Module createModule(String groupId, String artifactId, String artifactVersion) throws IOException {
        Module module = new Module(groupId, artifactId, artifactVersion);
        module.setRootClassLoader(ClassLoader.getSystemClassLoader());
        module.setClassLoader(new ModuleClassLoader(module));
        module.setClassStorage(new ClassStorageZipFileImpl(createPathToModuleJar(module)));
        buildDependencyGraph(module);
        return module;
    }

    public void buildDependencyGraph(Module module) throws IOException {
        //System.out.println("Building dependency graph for module: " + module.getFullName());

        List<Dependency> dependencies = readDependenciesForModule(module);

        List<Module> moduleDependencies = new ArrayList<Module>();

        for(Dependency dependency : dependencies) {
            if(NOT_RUNTIME_SCOPE.contains(dependency.scope)){
                continue;
            }
            Module moduleDependency = createModule(dependency.groupId, dependency.artifactId, dependency.version);
            moduleDependencies.add(moduleDependency);
        }

        module.setDependencies(moduleDependencies);

        for(Module moduleDependency : moduleDependencies){
            buildDependencyGraph(moduleDependency);
        }
    }

    public List<Dependency> readDependenciesForModule(Module module){
        String modulePomPath = createModulePomPath(module);

        try(Reader reader = new InputStreamReader(new FileInputStream(modulePomPath), "UTF-8")){
            return ModuleDependencyReader.readDependencies(reader);
        } catch (UnsupportedEncodingException e) {
            throw new ModRunException("Error reading dependencies for module " + module.getFullName(), e);
        } catch (FileNotFoundException e) {
            // Read the pom.xml file info from .jar
            modulePomPath = "META-INF/maven/" + module.getGroupId()+"/"+module.getArtifactId()+"/pom.xml";
            if (!module.getClassStorage().containsFile(modulePomPath)) {
                throw new ModRunException("Error reading dependencies for module " + module.getFullName(), e);
            }
            byte[] pomBytes;
            try {
                pomBytes = module.getClassStorage().readFileBytes(modulePomPath);
            } catch (IOException e2) {
                throw new ModRunException("Error reading dependencies for module " + module.getFullName(), e2);
            }
            try(InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(pomBytes))) {
                return new ModuleDependencyReader().readDependencies(reader);
            } catch (IOException e2) {
                throw new ModRunException("Error reading dependencies for module " + module.getFullName(), e2);
            }
        } catch (IOException e) {
            throw new ModRunException("Error reading dependencies for module " + module.getFullName(), e);
        }
    }

    private String createModulePomPath(Module module) {
        return this.rootDir + "/" + module.getFullName()
                            + "/" + module.getArtifactId()
                            + "-" + module.getVersion() + ".pom";
    }

    public File installModule(String remoteRepositoryBaseUrl, String groupId, String artifactId, String artifactVersion) throws IOException {
        ModuleDownloader moduleDownloader = new ModuleDownloader(remoteRepositoryBaseUrl, this.rootDir);
        return moduleDownloader.download(groupId, artifactId, artifactVersion);
    }

    public void installModuleAndDependencies(String remoteRepositoryBaseUrl, String groupId, String artifactId, String artifactVersion) throws IOException {
        installModule(remoteRepositoryBaseUrl, groupId, artifactId, artifactVersion);

        Module module = new Module(groupId, artifactId, artifactVersion);
        List<Dependency> dependencies = readDependenciesForModule(module);

        for(Dependency dependency : dependencies){
            if(NOT_RUNTIME_SCOPE.contains(dependency.scope)){
                continue;
            }
            installModuleAndDependencies(remoteRepositoryBaseUrl, dependency.groupId, dependency.artifactId, dependency.version);
        }
    }

    public void readDependencies(Module module, Function<Module, File> callback) {
        List<Dependency> dependencies = readDependenciesForModule(module);
        List<Module> moduleDependencies = new ArrayList<>();
        for(Dependency dependency : dependencies) {
            if(Repository.NOT_RUNTIME_SCOPE.contains(dependency.scope)){
                continue;
            }
            Module moduleDependency = new Module(dependency.groupId, dependency.artifactId, dependency.version);
            callback.apply(moduleDependency);
            moduleDependencies.add(moduleDependency);
        }
        module.setDependencies(moduleDependencies);
        for(Module moduleDependency : moduleDependencies){
            readDependencies(moduleDependency, callback);
        }
    }
}
