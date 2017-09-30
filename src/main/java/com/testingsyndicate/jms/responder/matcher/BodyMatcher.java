package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testingsyndicate.jms.responder.model.RequestInfo;

import java.util.Objects;

public final class BodyMatcher implements Matcher {

    private final String body;

    @JsonCreator
    public BodyMatcher(@JsonProperty("body") String body) {
        this.body = body;
    }

    public boolean matches(RequestInfo requestInfo) {
        String requestBody = requestInfo.getBody();
        return Objects.equals(body, requestBody);
    }

    public String getBody() {
        return body;
    }
}
