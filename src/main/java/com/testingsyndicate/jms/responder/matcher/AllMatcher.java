package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testingsyndicate.jms.responder.model.RequestInfo;

import java.util.Arrays;
import java.util.Collection;

public final class AllMatcher implements Matcher {

    private final Collection<Matcher> matchers;

    @JsonCreator
    public AllMatcher(@JsonProperty("matchers") Matcher... matchers) {
        this.matchers = Arrays.asList(matchers);
    }

    @Override
    public boolean matches(RequestInfo requestInfo) {
        return matchers
                .stream()
                .allMatch(m -> m.matches(requestInfo));
    }
}
