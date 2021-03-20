package com.testingsyndicate.jms.responder.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import com.testingsyndicate.jms.responder.model.BodySource;
import com.testingsyndicate.jms.responder.model.RequestInfo;
import org.junit.Test;

public class BodyMatcherTest {

  @Test
  public void matchesWhenEqual() {
    // given
    BodyMatcher sut = bodyMatcher("wibble", false);

    RequestInfo requestInfo = requestInfoWithBody("wibble");

    // when
    boolean actual = sut.matches(requestInfo);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  public void doesntMatchWhenDifferent() {
    // given
    BodyMatcher sut = bodyMatcher("wibble", false);
    RequestInfo requestInfo = requestInfoWithBody("wobble");

    // when
    boolean actual = sut.matches(requestInfo);

    // then
    assertThat(actual).isFalse();
  }

  @Test
  public void matchesNulls() {
    // given
    BodyMatcher sut = bodyMatcher(null, false);
    RequestInfo requestInfo = requestInfoWithBody(null);

    // when
    boolean actual = sut.matches(requestInfo);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  public void matchesWhenTrimmed() {
    // given
    BodyMatcher sut = bodyMatcher("hi", true);
    RequestInfo requestInfo = requestInfoWithBody("  hi   ");

    // when
    boolean actual = sut.matches(requestInfo);

    // then
    assertThat(actual).isTrue();
  }

  private static BodyMatcher bodyMatcher(String body, boolean trim) {
    return BodyMatcher.newBuilder().withBody(new BodySource(body)).withTrim(trim).build();
  }

  private static RequestInfo requestInfoWithBody(String body) {
    return RequestInfo.newBuilder().withBody(body).build();
  }
}
