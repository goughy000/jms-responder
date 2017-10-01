package com.testingsyndicate.jms.responder.model;

public final class RequestInfo {

    private final String body;
    private final String queueName;

    private RequestInfo(Builder builder) {
        body = builder.body;
        queueName = builder.queueName;
    }

    public String getBody() {
        return body;
    }

    public String getQueueName() {
        return queueName;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        private String body;
        private String queueName;

        private Builder() {
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Builder withQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public RequestInfo build() {
            return new RequestInfo(this);
        }

    }

}
