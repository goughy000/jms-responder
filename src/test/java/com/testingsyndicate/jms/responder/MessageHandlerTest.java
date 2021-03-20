package com.testingsyndicate.jms.responder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.testingsyndicate.jms.responder.model.BodySource;
import com.testingsyndicate.jms.responder.model.MatchableResponse;
import com.testingsyndicate.jms.responder.model.RequestInfo;
import com.testingsyndicate.jms.responder.repository.ResponseRepository;
import java.util.Optional;
import javax.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MessageHandlerTest {

  private ResponseRepository mockRepo;
  private TextMessage mockMessage;
  private Queue mockDestination;
  private Queue mockReplyDestination;
  private Session mockSession;
  private MessageProducer mockProducer;
  private TextMessage mockReplyMessage;

  private MessageHandler sut;

  @BeforeEach
  void beforeEach() throws JMSException {
    mockSession = mock(Session.class);
    mockRepo = mock(ResponseRepository.class);
    mockDestination = mock(Queue.class);
    mockReplyDestination = mock(Queue.class);
    mockMessage = mock(TextMessage.class);
    mockReplyMessage = mock(TextMessage.class);
    mockProducer = mock(MessageProducer.class);
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    when(mockMessage.getJMSDestination()).thenReturn(mockDestination);
    when(mockMessage.getJMSCorrelationID()).thenReturn("jms-corr-id");
    when(mockMessage.getJMSReplyTo()).thenReturn(mockReplyDestination);
    MatchableResponse response =
        MatchableResponse.newBuilder().withBody(new BodySource("bla the message")).build();
    when(mockRepo.findResponse(any(RequestInfo.class))).thenReturn(Optional.of(response));
    when(mockSession.createTextMessage()).thenReturn(mockReplyMessage);
    sut = new MessageHandler(mockSession, "q", mockRepo);
  }

  @Test
  void findsMatchFromRepo() throws JMSException {
    // given
    ArgumentCaptor<RequestInfo> captor = ArgumentCaptor.forClass(RequestInfo.class);
    when(mockMessage.getText()).thenReturn("wibble");
    when(mockDestination.getQueueName()).thenReturn("wobble");
    when(mockMessage.getJMSCorrelationID()).thenReturn("cobble");

    // when
    sut.onMessage(mockMessage);
    verify(mockRepo).findResponse(captor.capture());
    RequestInfo actual = captor.getValue();

    // then
    assertThat(actual.getBody()).isEqualTo("wibble");
    assertThat(actual.getQueueName()).isEqualTo("wobble");
    assertThat(actual.getCorrelationId()).isEqualTo("cobble");
  }

  @Test
  void sendsReplyToReplyTo() throws JMSException {
    // given

    // when
    sut.onMessage(mockMessage);

    // then
    verify(mockSession).createProducer(mockReplyDestination);
    verify(mockReplyMessage).setText("bla the message");
    verify(mockReplyMessage).setJMSCorrelationID("jms-corr-id");
    verify(mockReplyMessage).setJMSDestination(mockReplyDestination);
    verify(mockProducer).send(mockReplyMessage);
  }

  @Test
  void usesMessageIdIfNoCorrelationId() throws JMSException {
    // given
    when(mockMessage.getJMSCorrelationID()).thenReturn(null);
    when(mockMessage.getJMSMessageID()).thenReturn("jms-msg-id");

    // when
    sut.onMessage(mockMessage);

    // then
    verify(mockReplyMessage).setJMSCorrelationID("jms-msg-id");
  }

  @Test
  void doesntReplyIfNoMatch() throws JMSException {
    // given
    when(mockRepo.findResponse(any(RequestInfo.class))).thenReturn(Optional.empty());

    // when
    sut.onMessage(mockMessage);

    // then
    verify(mockSession, never()).createTextMessage();
  }

  @Test
  void doesntReplyIfNoReplyQueue() throws JMSException {
    // given
    when(mockMessage.getJMSReplyTo()).thenReturn(null);

    // when
    sut.onMessage(mockMessage);

    // then
    verify(mockMessage).getJMSReplyTo();
    verifyNoMoreInteractions(mockRepo);
  }
}
