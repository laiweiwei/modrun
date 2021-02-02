package com.jenkov.modrun;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * @author laiweiwei 2020-12-04
 */
public class ModulePomReader {

    private Dependency parent;
    private String name;
    private String packaging;
    private String groupId;
    private String artifactId;
    private String version;
    private Properties properties;
    private List<Dependency> dependencies;

    private ModulePomReader(Reader pomReader) {
        this.dependencies = new ArrayList<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader streamReader = factory.createXMLStreamReader(pomReader);
            while(streamReader.hasNext()){
                streamReader.next();
                if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
                    String elementName = streamReader.getLocalName();
                    if(elementName.equals("project")){
                        parseProject(streamReader);
                    } else if(elementName.equals("dependency")){
                        Dependency dependency = parseDependency(streamReader);
                        this.dependencies.add(dependency);
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public static ModulePomReader read(String pomFilePath) throws FileNotFoundException {
        FileReader reader = null;
        try {
            reader = new FileReader(pomFilePath);
            ModulePomReader pomReader = new ModulePomReader(reader);
            return pomReader;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static ModulePomReader read(Reader reader) {
        return new ModulePomReader(reader);
    }

    public void parseProject(XMLStreamReader streamReader) throws XMLStreamException {
        while(!(streamReader.getEventType() == XMLStreamReader.END_ELEMENT)){
            streamReader.next();
            if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                String elementName = streamReader.getLocalName();
                if("name".equals(elementName)){
                    this.name = streamReader.getElementText();
                } else if("packaging".equals(elementName)){
                    this.packaging = streamReader.getElementText();
                } else if("groupId".equals(elementName)){
                    this.groupId = streamReader.getElementText();
                } else if("artifactId".equals(elementName)){
                    this.artifactId = streamReader.getElementText();
                } else if("version".equals(elementName)){
                    this.version = streamReader.getElementText();
                } else if("parent".equals(elementName)){
                    Dependency parent = parseDependency(streamReader);
                    this.parent = parent;
                } else if(elementName.equals("properties")){
                    this.properties = parseProperties(streamReader);
                }
            }
        }
    }

    public static Properties parseProperties(XMLStreamReader streamReader) throws XMLStreamException {
        Properties properties = new Properties();
        while(!(streamReader.getEventType() == XMLStreamReader.END_ELEMENT)){
            streamReader.next();
            if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                String elementName = streamReader.getLocalName();
                properties.setProperty(elementName, streamReader.getElementText());
                while(streamReader.getEventType() != XMLStreamReader.END_ELEMENT){
                    streamReader.next();
                }
                streamReader.next();
            }
        }

        return properties;
    }

    public static Dependency parseDependency(XMLStreamReader streamReader) throws XMLStreamException {
        Dependency dependency = new Dependency();

        while(!(streamReader.getEventType() == XMLStreamReader.END_ELEMENT)){
            streamReader.next();

            if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                String elementName = streamReader.getLocalName();

                if("groupId".equals(elementName)){
                    dependency.groupId = streamReader.getElementText();
                    while(streamReader.getEventType() != XMLStreamReader.END_ELEMENT){
                        streamReader.next();
                    }
                    streamReader.next();
                } else if("artifactId".equals(elementName)){
                    dependency.artifactId = streamReader.getElementText();
                    while(streamReader.getEventType() != XMLStreamReader.END_ELEMENT){
                        streamReader.next();
                    }
                    streamReader.next();
                } else if("version".equals(elementName)){
                    dependency.version = streamReader.getElementText();
                    while(streamReader.getEventType() != XMLStreamReader.END_ELEMENT){
                        streamReader.next();
                    }
                    streamReader.next();
                } else if("scope".equals(elementName)){
                    dependency.scope = streamReader.getElementText();
                    while(streamReader.getEventType() != XMLStreamReader.END_ELEMENT){
                        streamReader.next();
                    }
                    streamReader.next();
                } else {
                    while(streamReader.getEventType() != XMLStreamReader.END_ELEMENT){
                        streamReader.next();
                    }
                    streamReader.next();
                }

            }
        }

        return dependency;
    }

    public String getName() {
        return replaceProps(name);
    }

    public String getPackaging() {
        return replaceProps(packaging);
    }

    public String getGroupId() {
        String gid = groupId == null ? ( parent != null ? parent.groupId : null ) : groupId;
        return replaceProps(gid);
    }

    public String getArtifactId() {
        String aid = artifactId == null ? ( parent != null ? parent.artifactId : null ) : artifactId;
        return replaceProps(aid);
    }

    public String getVersion() {
        String v = version == null ? ( parent != null ? parent.version : null ) : version;
        return replaceProps(v);
    }

    public Properties getProperties() {
        return properties;
    }

    public String replaceAll(String source) {
        if (source == null) {
            return source;
        }
        String groupId = this.getGroupId();
        String artifactId = this.getArtifactId();
        String version = this.getVersion();
        String name = this.getName();
        String packaging = this.getPackaging();

        String replacement = source;
        if (groupId != null) {
            replacement = replacement.replace("${project.groupId}", groupId);
        }
        if (artifactId != null) {
            replacement = replacement.replace("${project.artifactId}", artifactId);
        }
        if (version != null) {
            replacement = replacement.replace("${project.version}", version);
        }
        if (name != null) {
            replacement = replacement.replace("${project.name}", name);
        }
        if (packaging != null) {
            replacement = replacement.replace("${project.packaging}", packaging);
        }
        replacement = replaceProps(replacement);
        return replacement;
    }

    public String replaceProps(String source) {
        if (source == null) {
            return source;
        }
        String replacement = source;
        if (this.properties != null) {
            Enumeration<Object> keys = this.properties.keys();
            while (keys.hasMoreElements()) {
                String key = String.valueOf(keys.nextElement());
                replacement = replacement.replace("${"+key+"}", this.properties.getProperty(key));
            }
        }
        return replacement;
    }

    public List<Dependency> getDependencies() {
        List<Dependency> dependenciesCopy = new ArrayList<>();
        if (this.dependencies != null) {
            for (Dependency dependency : dependencies) {
                Dependency depCopy = new Dependency();
                depCopy.groupId = replaceAll(dependency.groupId);
                depCopy.artifactId = replaceAll(dependency.artifactId);
                depCopy.version = replaceAll(dependency.version);
                depCopy.scope = replaceAll(dependency.scope);
                dependenciesCopy.add(depCopy);
            }
        }
        return dependenciesCopy;
    }

    @Override
    public String toString() {
        return "ModulePomReader{" +
                "parent=" + parent +
                ", name='" + getName() + '\'' +
                ", packaging='" + getPackaging() + '\'' +
                ", groupId='" + getGroupId() + '\'' +
                ", artifactId='" + getArtifactId() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", properties=" + properties +
                ", dependencies=" + getDependencies() +
                '}';
    }

    public static void main(String[] args) throws Exception {
        String repoRootDir = "C:\\Users\\laiweiwei\\.m2\\repository";
        String filePath = "\\org\\springframework\\spring-aop\\5.3.1\\spring-aop-5.3.1.pom";
        filePath = "\\io\\netty\\netty-all\\4.1.35.Final\\netty-all-4.1.35.Final.pom";
        String pomFilePath = repoRootDir + filePath;
        pomFilePath = "I:\\dev\\projects\\xfaas\\xfaas-js\\src\\app\\module-upload\\repo\\com\\cmschina\\zbus-java-demo\\1.0-SNAPSHOT\\pom.xml";
        ModulePomReader pomReader = ModulePomReader.read(pomFilePath);
        pomReader.getDependencies().forEach(System.out::println);
    }

}
