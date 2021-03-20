package com.testingsyndicate.jms.responder.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class AllMatcherTest {

  private Matcher falseMatcher;
  private Matcher trueMatcher;

  @Before
  public void setup() {
    falseMatcher = mock(Matcher.class);
    trueMatcher = mock(Matcher.class);

    when(falseMatcher.matches(any())).thenReturn(false);
    when(trueMatcher.matches(any())).thenReturn(true);
  }

  @Test
  public void matchesWhenTrue() {
    // given
    AllMatcher sut = new AllMatcher(trueMatcher);

    // when
    boolean actual = sut.matches(null);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  public void doesntMatchWhenFalse() {
    // given
    AllMatcher sut = new AllMatcher(falseMatcher);

    // when
    boolean actual = sut.matches(null);

    // then
    assertThat(actual).isFalse();
  }

  @Test
  public void matchesWhenAllTrue() {
    // given
    AllMatcher sut = new AllMatcher(trueMatcher, trueMatcher);

    // when
    boolean actual = sut.matches(null);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  public void doesntMatchWhenOneFalse() {
    // given
    AllMatcher sut = new AllMatcher(trueMatcher, trueMatcher, falseMatcher);

    // when
    boolean actual = sut.matches(null);

    // then
    assertThat(actual).isFalse();
  }
}
