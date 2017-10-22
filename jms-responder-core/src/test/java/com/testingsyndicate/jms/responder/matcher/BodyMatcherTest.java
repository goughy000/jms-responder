package com.testingsyndicate.jms.responder.matcher;

import com.testingsyndicate.jms.responder.model.RequestInfo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BodyMatcherTest {

    @Test
    public void matchesWhenEqual() {
        // given
        BodyMatcher sut = BodyMatcher.newBuilder().withBody("wibble").build();

        RequestInfo requestInfo = requestInfoWithBody("wibble");

        // when
        boolean actual = sut.matches(requestInfo);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void doesntMatchWhenDifferent() {
        // given
        BodyMatcher sut = BodyMatcher.newBuilder().withBody("wibble").build();
        RequestInfo requestInfo = requestInfoWithBody("wobble");

        // when
        boolean actual = sut.matches(requestInfo);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void matchesNulls() {
        // given
        BodyMatcher sut = BodyMatcher.newBuilder().withBody(null).build();
        RequestInfo requestInfo = requestInfoWithBody(null);

        // when
        boolean actual = sut.matches(requestInfo);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void matchesWhenTrimmed() {
        // given
        BodyMatcher sut = BodyMatcher.newBuilder().withBody("hi").withTrim(true).build();
        RequestInfo requestInfo = requestInfoWithBody("  hi   ");

        // when
        boolean actual = sut.matches(requestInfo);

        // then
        assertThat(actual).isTrue();
    }

    private static RequestInfo requestInfoWithBody(String body) {
        return RequestInfo.newBuilder()
                .withBody(body)
                .build();
    }

}