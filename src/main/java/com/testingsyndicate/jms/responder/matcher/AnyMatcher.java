package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testingsyndicate.jms.responder.model.Request;

import java.util.Arrays;
import java.util.Collection;

public final class AnyMatcher implements Matcher {

    private final Collection<Matcher> matchers;

    @JsonCreator
    public AnyMatcher(@JsonProperty("matchers") Matcher... matchers) {
        this.matchers = Arrays.asList(matchers);
    }

    @Override
    public boolean matches(Request request) {
        return matchers
                .stream()
                .anyMatch(m -> m.matches(request));
    }
}
