package com.testingsyndicate.jms.responder.model;

public final class RequestInfo implements Request {

    private final String body;
    private final String queueName;
    private final String correlationId;

    private RequestInfo(Builder builder) {
        body = builder.body;
        queueName = builder.queueName;
        correlationId = builder.correlationId;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public String toString() {
        return String.format("Request (CorrelationId=%s) (QueueName=%s)",
                correlationId,
                queueName);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        private String body;
        private String queueName;
        private String correlationId;

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

        public Builder withCorrelationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public RequestInfo build() {
            return new RequestInfo(this);
        }

    }

}
