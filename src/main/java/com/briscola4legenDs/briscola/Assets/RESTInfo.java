package com.briscola4legenDs.briscola.Assets;

public class RESTInfo {
    private final String path;
    private final String method;
    private final String name;
    private final String args;
    private final String description;

    public RESTInfo(String path, String method, String name, String args, String description) {
        this.path = path;
        this.method = method;
        this.name = name;
        this.args = args;
        this.description = description;
    }

    public RESTInfo(String path, String method, String name, String description) {
        this.path = path;
        this.method = method;
        this.name = name;
        this.args = null;
        this.description = description;
    }

    public RESTInfo(String path, String method, String name) {
        this.path = path;
        this.method = method;
        this.name = name;
        args = null;
        description = null;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public String getArgs() {
        return args;
    }

    public String getDescription() {
        return description;
    }
}
