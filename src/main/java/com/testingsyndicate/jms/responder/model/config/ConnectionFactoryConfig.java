package com.testingsyndicate.jms.responder.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public final class ConnectionFactoryConfig {

  private final String clazz;
  private final List<Object> arguments;
  private final Map<String, String> properties;

  public ConnectionFactoryConfig(
      @JsonProperty("class") String clazz,
      @JsonProperty("arguments") List<Object> arguments,
      @JsonProperty("properties") Map<String, String> properties) {
    this.clazz = clazz;
    this.arguments = arguments;
    this.properties = properties;
  }

  public String getClazz() {
    return clazz;
  }

  public List<Object> getArguments() {
    return arguments;
  }

  public Map<String, String> getProperties() {
    return properties;
  }
}
