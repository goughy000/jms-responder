package com.testingsyndicate.jms.responder;

import com.testingsyndicate.jms.responder.model.RequestInfo;
import com.testingsyndicate.jms.responder.model.StubbedResponse;
import com.testingsyndicate.jms.responder.repository.ResponseRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.jms.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageHandlerTest {

    private ResponseRepository mockRepo;
    private TextMessage mockMessage;
    private Queue mockDestination;
    private Queue mockReplyDestination;
    private Session mockSession;
    private MessageProducer mockProducer;
    private TextMessage mockReplyMessage;

    private MessageHandler sut;

    @Before
    public void setup() throws JMSException {
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
        StubbedResponse response = StubbedResponse.newBuilder().withBody("bla the message").build();
        when(mockRepo.findMatch(any(RequestInfo.class))).thenReturn(Optional.of(response));
        when(mockSession.createTextMessage()).thenReturn(mockReplyMessage);
        sut = new MessageHandler(mockSession, "q", mockRepo);
    }

    @Test
    public void findsMatchFromRepo() throws JMSException {
        // given
        ArgumentCaptor<RequestInfo> captor = ArgumentCaptor.forClass(RequestInfo.class);
        when(mockMessage.getText()).thenReturn("wibble");
        when(mockDestination.getQueueName()).thenReturn("wobble");
        when(mockMessage.getJMSCorrelationID()).thenReturn("cobble");

        // when
        sut.onMessage(mockMessage);
        verify(mockRepo).findMatch(captor.capture());
        RequestInfo actual = captor.getValue();

        // then
        assertThat(actual.getBody()).isEqualTo("wibble");
        assertThat(actual.getQueueName()).isEqualTo("wobble");
        assertThat(actual.getCorrelationId()).isEqualTo("cobble");
    }

    @Test
    public void sendsReplyToReplyTo() throws JMSException {
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
    public void usesMessageIdIfNoCorrelationId() throws JMSException {
        // given
        when(mockMessage.getJMSCorrelationID()).thenReturn(null);
        when(mockMessage.getJMSMessageID()).thenReturn("jms-msg-id");

        // when
        sut.onMessage(mockMessage);

        // then
        verify(mockReplyMessage).setJMSCorrelationID("jms-msg-id");
    }

    @Test
    public void doesntReplyIfNoMatch() throws JMSException {
        // given
        when(mockRepo.findMatch(any(RequestInfo.class))).thenReturn(Optional.empty());

        // when
        sut.onMessage(mockMessage);

        // then
        verify(mockSession, never()).createTextMessage();
    }

}
