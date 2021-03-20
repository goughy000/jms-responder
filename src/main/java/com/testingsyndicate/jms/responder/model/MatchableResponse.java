package com.testingsyndicate.jms.responder.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.testingsyndicate.jms.responder.matcher.Matcher;
import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(builder = MatchableResponse.Builder.class)
public final class MatchableResponse implements Response {

  private final String description;
  private final List<Matcher> matchers;
  private final String body;
  private final int delay;

  private MatchableResponse(Builder builder) {
    description = builder.description;
    matchers = builder.matchers;
    body = builder.body;
    delay = builder.delay;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public int getDelay() {
    return delay;
  }

  public List<Matcher> getMatchers() {
    return matchers;
  }

  @Override
  public String toString() {
    return String.format("Response (Description=%s) (Delay=%s)", description, delay);
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

    public MatchableResponse build() {
      return new MatchableResponse(this);
    }
  }
}
