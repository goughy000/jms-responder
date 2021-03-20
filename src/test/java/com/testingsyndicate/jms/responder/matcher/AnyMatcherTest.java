package com.testingsyndicate.jms.responder.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnyMatcherTest {

  private Matcher falseMatcher;
  private Matcher trueMatcher;

  @BeforeEach
  void beforeEach() {
    falseMatcher = mock(Matcher.class);
    trueMatcher = mock(Matcher.class);

    when(falseMatcher.matches(any())).thenReturn(false);
    when(trueMatcher.matches(any())).thenReturn(true);
  }

  @Test
  void matchesWhenTrue() {
    // given
    AnyMatcher sut = new AnyMatcher(trueMatcher);

    // when
    boolean actual = sut.matches(null);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  void doesntMatchWhenFalse() {
    // given
    AnyMatcher sut = new AnyMatcher(falseMatcher);

    // when
    boolean actual = sut.matches(null);

    // then
    assertThat(actual).isFalse();
  }

  @Test
  void matchesWhenAllTrue() {
    // given
    AnyMatcher sut = new AnyMatcher(trueMatcher, trueMatcher);

    // when
    boolean actual = sut.matches(null);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  void matchesWhenOneTrue() {
    // given
    AnyMatcher sut = new AnyMatcher(falseMatcher, falseMatcher, trueMatcher);

    // when
    boolean actual = sut.matches(null);

    // then
    assertThat(actual).isTrue();
  }
}
