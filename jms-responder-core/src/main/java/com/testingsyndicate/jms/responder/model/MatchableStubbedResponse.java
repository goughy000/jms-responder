package com.testingsyndicate.jms.responder.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.testingsyndicate.jms.responder.matcher.Matcher;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(builder = MatchableStubbedResponse.Builder.class)
public final class MatchableStubbedResponse implements StubbedResponse {

    private final String description;
    private final List<Matcher> matchers;
    private final String body;
    private final int delay;

    private MatchableStubbedResponse(Builder builder) {
        description = builder.description;
        matchers = builder.matchers;
        body = builder.body;
        delay = builder.delay;
    }

    public String getDescription() {
        return description;
    }

    public List<Matcher> getMatchers() {
        return matchers;
    }

    public String getBody() {
        return body;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return String.format("Response (Description=%s) (Delay=%s)",
                description,
                delay);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static final class Builder {

        private String description;
        private List<Matcher> matchers;
        private String body;
        private int delay;

        private Builder() {
            matchers = new ArrayList<>();
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withMatcher(Matcher matcher) {
            this.matchers.add(matcher);
            return this;
        }

        public Builder withMatchers(List<Matcher> matchers) {
            this.matchers = matchers;
            return this;
        }

        public Builder withBody(BodySource body) {
            this.body = body.getBody();
            return this;
        }

        public Builder withDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public MatchableStubbedResponse build() {
            return new MatchableStubbedResponse(this);
        }

    }

}
