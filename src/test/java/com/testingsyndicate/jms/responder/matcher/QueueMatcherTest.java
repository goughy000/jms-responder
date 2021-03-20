package com.testingsyndicate.jms.responder.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import com.testingsyndicate.jms.responder.model.RequestInfo;
import org.junit.jupiter.api.Test;

class QueueMatcherTest {

  @Test
  void matchesWhenEqual() {
    // given
    QueueMatcher sut = new QueueMatcher("wibble");

    RequestInfo requestInfo = requestInfoWithQueue("wibble");

    // when
    boolean actual = sut.matches(requestInfo);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  void doesntMatchWhenDifferent() {
    // Given
    QueueMatcher sut = new QueueMatcher("wibble");
    RequestInfo requestInfo = requestInfoWithQueue("wobble");

    // when
    boolean actual = sut.matches(requestInfo);

    // then
    assertThat(actual).isFalse();
  }

  @Test
  void matchesNulls() {
    // Given
    QueueMatcher sut = new QueueMatcher(null);
    RequestInfo requestInfo = requestInfoWithQueue(null);

    // when
    boolean actual = sut.matches(requestInfo);

    // then
    assertThat(actual).isTrue();
  }

  private static RequestInfo requestInfoWithQueue(String queue) {
    return RequestInfo.newBuilder().withQueueName(queue).build();
  }
}
