package com.testingsyndicate.jms.responder.matcher;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnyMatcherTest {

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
        AnyMatcher sut = new AnyMatcher(trueMatcher);

        // when
        boolean actual = sut.matches(null);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void doesntMatchWhenFalse() {
        // given
        AnyMatcher sut = new AnyMatcher(falseMatcher);

        // when
        boolean actual = sut.matches(null);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void matchesWhenAllTrue() {
        // given
        AnyMatcher sut = new AnyMatcher(trueMatcher, trueMatcher);

        // when
        boolean actual = sut.matches(null);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void matchesWhenOneTrue() {
        // given
        AnyMatcher sut = new AnyMatcher(falseMatcher, falseMatcher, trueMatcher);

        // when
        boolean actual = sut.matches(null);

        // then
        assertThat(actual).isTrue();
    }


}