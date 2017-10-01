package com.testingsyndicate.jms.responder;

import com.testingsyndicate.jms.responder.model.RequestInfo;
import com.testingsyndicate.jms.responder.model.StubbedResponse;
import com.testingsyndicate.jms.responder.repository.ResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.jms.IllegalStateException;
import java.util.Optional;

final class MessageHandler implements Runnable {

    private static final int RECEIVE_TIMEOUT = 2000;

    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

    private final Session session;
    private final String queueName;
    private final ResponseRepository repository;;

    MessageHandler(Session session, String queueName, ResponseRepository repository) {
        this.session = session;
        this.queueName = queueName;
        this.repository = repository;
    }

    @Override
    public void run() {
        MessageConsumer consumer = null;

        try {
            Destination destination = session.createQueue(queueName);
            consumer = session.createConsumer(destination);

            while (true) {
                try {
                    LOG.info("Waiting for message on {} for {}ms", destination, RECEIVE_TIMEOUT);
                    Message request = consumer.receive(RECEIVE_TIMEOUT);
                    if (null != request) {
                        onMessage(request);
                    }
                } catch (IllegalStateException ise) {
                    LOG.info("Shutting down");
                    break;
                } catch (JMSException ex) {
                    LOG.warn("Error in message loop, {}", ex.getMessage());
                }
            }

        } catch (JMSException ex) {
            LOG.error("Error in thread startup", ex);
            throw new RuntimeException("Error on startup", ex);
        } finally {
            if (null != consumer) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    LOG.error("Could not close consumer", e);
                }
            }
        }
    }

    void onMessage(Message message) {

        LOG.info("Received message {}", message);

        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;

                // Find a stub
                Queue queue = (Queue) textMessage.getJMSDestination();

                RequestInfo requestInfo = RequestInfo.newBuilder()
                        .withBody(textMessage.getText())
                        .withQueueName(queue.getQueueName())
                        .build();

                LOG.trace("Looking for a match");
                Optional<StubbedResponse> match = repository.findMatch(requestInfo);

                // Did we find one?
                if (match.isPresent()) {
                    StubbedResponse response = match.get();
                    LOG.debug("Found match {}", response);

                    // Work out correlation id for the reply
                    String correlationId = textMessage.getJMSCorrelationID();
                    if (null == correlationId) {
                        LOG.debug("Falling back to the MessageID for CorrelationID");
                        correlationId = textMessage.getJMSMessageID();
                    }
                    LOG.trace("Reply CorrelationID = {}", correlationId);

                    // Where are we sending?
                    Destination replyTo = textMessage.getJMSReplyTo();
                    LOG.debug("ReplyTo = {}", replyTo);

                    // Build the reply message
                    TextMessage reply = session.createTextMessage();
                    reply.setJMSDestination(replyTo);
                    reply.setText(response.getBody());
                    reply.setJMSCorrelationID(correlationId);

                    // Sleep
                    int delay = response.getDelay();
                    if (delay > 0) {
                        LOG.debug("Sleeping for {}ms", delay);
                        Thread.sleep(delay);
                    }

                    // Send!
                    MessageProducer producer = null;
                    try {
                        producer = session.createProducer(replyTo);
                        LOG.info("Sending reply");
                        producer.send(reply);
                        LOG.debug("Reply sent");
                    } finally {
                        if (null != producer) {
                            producer.close();
                        }
                    }
                } else {
                    LOG.warn("No match found, not sending a reply");
                }
            } else {
                LOG.info("Ignoring message (Not TextMessage, is {})", message.getClass());
            }

        } catch (InterruptedException | JMSException e) {
            LOG.error("Error in onMessage", e);
        }
    }
}
