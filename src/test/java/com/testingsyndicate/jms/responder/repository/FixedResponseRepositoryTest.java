package com.testingsyndicate.jms.responder.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.testingsyndicate.jms.responder.matcher.Matcher;
import com.testingsyndicate.jms.responder.model.MatchableResponse;
import com.testingsyndicate.jms.responder.model.RequestInfo;
import com.testingsyndicate.jms.responder.model.Response;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

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
    Optional<Response> actual = sut.findResponse(dummyRequestInfo);

    // then
    assertThat(actual).isEmpty();
  }

  @Test
  public void returnsStubWhenAllMatch() {
    // given
    MatchableResponse response = response(trueMatcher);
    ResponseRepository sut = repo(response);

    // when
    Optional<Response> actual = sut.findResponse(dummyRequestInfo);

    // then
    assertThat(actual).contains(response);
  }

  @Test
  public void returnsEmptyWhenPartialMatch() {
    // given
    ResponseRepository sut = repo(response(trueMatcher, falseMatcher));

    // when
    Optional<Response> actual = sut.findResponse(dummyRequestInfo);

    // then
    assertThat(actual).isEmpty();
  }

  @Test
  public void shortCircuitsWhenPartialMatch() {
    // given
    ResponseRepository sut = repo(response(falseMatcher, trueMatcher));

    // when
    Optional<Response> actual = sut.findResponse(dummyRequestInfo);

    // then
    verify(falseMatcher).matches(eq(dummyRequestInfo));
    verify(trueMatcher, never()).matches(any());
    assertThat(actual).isEmpty();
  }

  @Test
  public void returnsSecondStubIfMatch() {
    // given
    MatchableResponse response = response(trueMatcher);
    ResponseRepository sut = repo(response(falseMatcher), response);

    // when
    Optional<Response> actual = sut.findResponse(dummyRequestInfo);

    // then
    assertThat(actual).contains(response);
  }

  @Test
  public void returnsResponseIfEmptyMatchers() {
    // given
    MatchableResponse response = response();
    ResponseRepository sut = repo(response);

    // when
    Optional<Response> actual = sut.findResponse(dummyRequestInfo);

    // then
    assertThat(actual).contains(response);
  }

  private static MatchableResponse response(Matcher... matchers) {
    return MatchableResponse.newBuilder().withMatchers(Arrays.asList(matchers)).build();
  }

  private static FixedResponseRepository repo(MatchableResponse... responses) {
    return new FixedResponseRepository(Arrays.asList(responses));
  }
}
