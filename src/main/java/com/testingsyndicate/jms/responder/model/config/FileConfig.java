package com.testingsyndicate.jms.responder.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testingsyndicate.jms.responder.model.StubbedResponse;

import java.util.List;

public final class FileConfig {

    private final ConnectionFactoryConfig connectionFactory;
    private final List<String> queues;
    private final List<StubbedResponse> stubs;

    @JsonCreator
    public FileConfig(@JsonProperty("connectionFactory") ConnectionFactoryConfig connectionFactory,
                      @JsonProperty("queues") List<String> queues,
                      @JsonProperty("stubs") List<StubbedResponse> stubs) {
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

    public List<StubbedResponse> getStubs() {
        return stubs;
    }
}
