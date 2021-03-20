package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.testingsyndicate.jms.responder.model.BodySource;
import com.testingsyndicate.jms.responder.model.Request;
import java.util.Objects;

@JsonDeserialize(builder = BodyMatcher.Builder.class)
public final class BodyMatcher implements Matcher {

  private final String body;
  private final boolean trim;

  private BodyMatcher(Builder builder) {
    trim = builder.trim;
    if (null != builder.body && trim) {
      body = builder.body.trim();
    } else {
      body = builder.body;
    }
  }

  public boolean matches(Request request) {
    String requestBody = request.getBody();

    if (null != requestBody && trim) {
      requestBody = requestBody.trim();
    }

    return Objects.equals(body, requestBody);
  }

  public String getBody() {
    return body;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @JsonPOJOBuilder
  public static final class Builder {

    private String body;
    private boolean trim;

    private Builder() {
      trim = false;
    }

    public Builder withBody(BodySource body) {
      this.body = body.getBody();
      return this;
    }

    public Builder withTrim(boolean trim) {
      this.trim = trim;
      return this;
    }

    public BodyMatcher build() {
      return new BodyMatcher(this);
    }
  }
}
