package com.testingsyndicate.jms.responder.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.testingsyndicate.jms.responder.model.BodySource;
import com.testingsyndicate.jms.responder.model.MatchableResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class FileConfig {

    private final ConnectionFactoryConfig connectionFactory;
    private final List<String> queues;
    private final List<MatchableResponse> stubs;

    @JsonCreator
    public FileConfig(@JsonProperty("connectionFactory") ConnectionFactoryConfig connectionFactory,
                      @JsonProperty("queues") List<String> queues,
                      @JsonProperty("stubs") List<MatchableResponse> stubs) {
        this.connectionFactory = connectionFactory;
        this.queues = queues;
        this.stubs = stubs;
    }

    public ConnectionFactoryConfig getConnectionFactory() {
        return connectionFactory;
    }

    public List<String> getQueues() {
        return queues;
    }

    public List<MatchableResponse> getStubs() {
        return stubs;
    }

    public static FileConfig fromFile(String path) throws IOException {
        return fromFile(new File(path));
    }

    public static FileConfig fromFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BodySource.class, new BodySourceDeserializer(file.getParent()));
        mapper.registerModule(module);

        return mapper.readValue(file, FileConfig.class);
    }
}
