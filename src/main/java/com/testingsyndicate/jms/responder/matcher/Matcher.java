package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.testingsyndicate.jms.responder.model.Request;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @Type(value = BodyMatcher.class, name = "body"),
  @Type(value = QueueMatcher.class, name = "queue"),
  @Type(value = XmlMatcher.class, name = "xml"),
  @Type(value = AnyMatcher.class, name = "any"),
  @Type(value = AllMatcher.class, name = "all")
})
public interface Matcher {

  boolean matches(Request request);
}
