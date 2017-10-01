package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.testingsyndicate.jms.responder.model.RequestInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = BodyMatcher.class, name = "body"),
                @JsonSubTypes.Type(value = QueueMatcher.class, name = "queue"),
                @JsonSubTypes.Type(value = AnyMatcher.class, name = "any"),
                @JsonSubTypes.Type(value = AllMatcher.class, name = "all")
        }
)
public interface Matcher {

    boolean matches(RequestInfo requestInfo);

}
