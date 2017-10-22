package com.testingsyndicate.jms.responder.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public final class ConnectionFactoryConfig {

    private final String clazz;
    private final Map<String, String> properties;

    public ConnectionFactoryConfig(@JsonProperty("class") String clazz,
                                   @JsonProperty("properties") Map<String, String> properties) {
        this.clazz = clazz;
        this.properties = properties;
    }

    public String getClazz() {
        return clazz;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
