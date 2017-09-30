package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.testingsyndicate.jms.responder.model.RequestInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = BodyMatcher.class),
                @JsonSubTypes.Type(value = QueueMatcher.class)
        }
)
public interface Matcher {

    boolean matches(RequestInfo requestInfo);

}
