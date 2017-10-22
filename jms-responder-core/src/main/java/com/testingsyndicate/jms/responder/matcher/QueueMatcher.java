package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testingsyndicate.jms.responder.model.RequestInfo;

import java.util.Objects;

public final class QueueMatcher implements Matcher {

    private final String queue;

    @JsonCreator
    public QueueMatcher(@JsonProperty("queue") String queue) {
        this.queue = queue;
    }

    @Override
    public boolean matches(RequestInfo requestInfo) {
        String requestQueue = requestInfo.getQueueName();
        return Objects.equals(queue, requestQueue);
    }
}
