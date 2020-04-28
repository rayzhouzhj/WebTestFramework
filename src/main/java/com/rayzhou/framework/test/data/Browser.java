package com.rayzhou.framework.test.data;

public enum Browser {
    RANDOM("RANDOM"),
    CHROME("CHROME"),
    FIREFOX("FIREFOX");

    private String name;
    Browser(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
