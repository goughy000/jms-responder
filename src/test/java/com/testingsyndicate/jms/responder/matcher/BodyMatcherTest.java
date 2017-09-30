package com.testingsyndicate.jms.responder.matcher;

import com.testingsyndicate.jms.responder.model.RequestInfo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BodyMatcherTest {

    @Test
    public void matchesWhenEqual() {
        // given
        BodyMatcher sut = new BodyMatcher("wibble");

        RequestInfo requestInfo = requestInfoWithBody("wibble");

        // when
        boolean actual = sut.matches(requestInfo);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void doesntMatchWhenDifferent() {
        // Given
        BodyMatcher sut = new BodyMatcher("wibble");
        RequestInfo requestInfo = requestInfoWithBody("wobble");

        // when
        boolean actual = sut.matches(requestInfo);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void matchesNulls() {
        // Given
        BodyMatcher sut = new BodyMatcher(null);
        RequestInfo requestInfo = requestInfoWithBody(null);

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