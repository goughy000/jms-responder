package com.testingsyndicate.jms.responder.model;

public interface Request {

    String getBody();

    String getQueueName();

    String getCorrelationId();

}
