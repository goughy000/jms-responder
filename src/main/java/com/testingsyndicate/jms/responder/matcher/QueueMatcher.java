package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testingsyndicate.jms.responder.model.Request;

import java.util.Objects;

public final class QueueMatcher implements Matcher {

    private final String queue;

    @JsonCreator
    public QueueMatcher(@JsonProperty("queue") String queue) {
        this.queue = queue;
    }

    @Override
    public boolean matches(Request request) {
        String requestQueue = request.getQueueName();
        return Objects.equals(queue, requestQueue);
    }
}
