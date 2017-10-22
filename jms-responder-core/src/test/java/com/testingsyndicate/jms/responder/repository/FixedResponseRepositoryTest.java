package com.testingsyndicate.jms.responder.repository;

import com.testingsyndicate.jms.responder.matcher.Matcher;
import com.testingsyndicate.jms.responder.model.RequestInfo;
import com.testingsyndicate.jms.responder.model.StubbedResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class FixedResponseRepositoryTest {

    private Matcher falseMatcher;
    private Matcher trueMatcher;
    private RequestInfo dummyRequestInfo;

    @Before
    public void setup() {
        falseMatcher = mock(Matcher.class);
        trueMatcher = mock(Matcher.class);

        when(falseMatcher.matches(any(RequestInfo.class))).thenReturn(false);
        when(trueMatcher.matches(any(RequestInfo.class))).thenReturn(true);

        dummyRequestInfo = RequestInfo.newBuilder().build();
    }

    @Test
    public void returnsEmptyWhenNoMatches() {
        // given
        ResponseRepository sut = repo(response(falseMatcher));

        // when
        Optional<StubbedResponse> actual = sut.findMatch(dummyRequestInfo);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    public void returnsStubWhenAllMatch() {
        // given
        StubbedResponse response = response(trueMatcher);
        ResponseRepository sut = repo(response);

        // when
        Optional<StubbedResponse> actual = sut.findMatch(dummyRequestInfo);

        // then
        assertThat(actual).contains(response);
    }

    @Test
    public void returnsEmptyWhenPartialMatch() {
        // given
        ResponseRepository sut = repo(response(trueMatcher, falseMatcher));

        // when
        Optional<StubbedResponse> actual = sut.findMatch(dummyRequestInfo);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    public void shortCircuitsWhenPartialMatch() {
        // given
        ResponseRepository sut = repo(response(falseMatcher, trueMatcher));

        // when
        Optional<StubbedResponse> actual = sut.findMatch(dummyRequestInfo);

        // then
        verify(falseMatcher).matches(eq(dummyRequestInfo));
        verify(trueMatcher, never()).matches(any());
        assertThat(actual).isEmpty();
    }

    @Test
    public void returnsSecondStubIfMatch() {
        // given
        StubbedResponse response = response(trueMatcher);
        ResponseRepository sut = repo(response(falseMatcher), response);

        // when
        Optional<StubbedResponse> actual = sut.findMatch(dummyRequestInfo);

        // then
        assertThat(actual).contains(response);
    }

    @Test
    public void returnsResponseIfEmptyMatchers() {
        // given
        StubbedResponse response = response();
        ResponseRepository sut = repo(response);

        // when
        Optional<StubbedResponse> actual = sut.findMatch(dummyRequestInfo);

        // then
        assertThat(actual).contains(response);
    }

    private static StubbedResponse response(Matcher... matchers) {
        return StubbedResponse.newBuilder()
                .withMatchers(Arrays.asList(matchers))
                .build();
    }

    private static FixedResponseRepository repo(StubbedResponse... responses) {
        return new FixedResponseRepository(Arrays.asList(responses));
    }

}