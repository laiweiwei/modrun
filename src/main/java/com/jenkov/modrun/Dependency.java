package com.jenkov.modrun;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jjenkov on 31-10-2016.
 * @author laiweiwei 2020-12-04
 */
public class Dependency {

    public String groupId;
    public String artifactId;
    public String version;
    public String scope;
    public List<Dependency> children;

    public Dependency() {
        this.children = new ArrayList<>();
    }

    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.children = new ArrayList<>();
    }

//    public boolean isRuntimeDependency() {
//        if("test".equals(this.scope)){
//            return false;
//        }
//        return true;
//    }

    public String toString() {
        return "(groupId: " + groupId + ", artifactId: " + artifactId + ", version: " + version + ", scope: " + scope + ")";
    }


}
