package com.testingsyndicate.jms.responder;

import com.testingsyndicate.jms.responder.model.RequestInfo;
import com.testingsyndicate.jms.responder.model.Response;
import com.testingsyndicate.jms.responder.repository.ResponseRepository;
import java.util.Optional;
import javax.jms.*;
import javax.jms.IllegalStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MessageHandler implements Runnable {

  private static final int RECEIVE_TIMEOUT = 2000;

  private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

  private final Session session;
  private final String queueName;
  private final ResponseRepository repository;

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

      for (; ; ) {
        try {
          LOG.info("Waiting for message on {} for {}ms", destination, RECEIVE_TIMEOUT);
          Message request = consumer.receive(RECEIVE_TIMEOUT);
          if (null != request) {
            onMessage(request);
          }
        } catch (IllegalStateException ise) {
          break;
        } catch (JMSException ex) {
          if (ex.getCause() instanceof InterruptedException) {
            break;
          }
          LOG.warn("Error in message loop, {}", ex.getMessage());
        }
      }

      LOG.info("Shutting down");

    } catch (JMSException ex) {
      LOG.error("Error in thread startup", ex);
      throw new RuntimeException("Error on startup", ex);
    } finally {
      if (null != consumer) {
        try {
          consumer.close();
        } catch (JMSException e) {
          LOG.error("Could not close consumer");
        }
      }
    }
  }

  void onMessage(Message message) {

    LOG.debug("Received message {}", message);

    try {
      if (!(message instanceof TextMessage)) {
        LOG.info("Ignoring message (Not TextMessage, is {})", message.getClass());
        return;
      }

      TextMessage textMessage = (TextMessage) message;

      // Grab info
      Queue queue = (Queue) textMessage.getJMSDestination();
      Queue replyTo = (Queue) textMessage.getJMSReplyTo();
      String queueName = queue.getQueueName();
      String body = textMessage.getText();
      String correlationId = textMessage.getJMSCorrelationID();

      LOG.info(">>> (CorrelationId={}) (Queue={})", correlationId, queueName);
      LOG.trace(">>> {}", body);

      if (null == replyTo) {
        LOG.warn("No reply queue, not sending a reply");
        return;
      }

      // Find a stub
      LOG.trace("Looking for a match");
      RequestInfo requestInfo =
          RequestInfo.newBuilder()
              .withBody(body)
              .withQueueName(queueName)
              .withCorrelationId(correlationId)
              .build();

      Optional<Response> match = repository.findResponse(requestInfo);

      // Did we find one?
      if (!match.isPresent()) {
        LOG.warn("No match found, not sending a reply");
        return;
      }

      Response response = match.get();
      LOG.trace("Found match {}", response);

      // Work out correlation id for the reply
      if (null == correlationId) {
        LOG.debug("Falling back to the MessageID for CorrelationID");
        correlationId = textMessage.getJMSMessageID();
      }

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
        LOG.info(
            "<<< (CorrelationId={}) (Queue={}) (Description={})",
            correlationId,
            replyTo.getQueueName(),
            response.getDescription());
        LOG.trace("<<< {}", response.getBody());
        producer.send(reply);
        LOG.debug("Reply sent");
      } finally {
        if (null != producer) {
          producer.close();
        }
      }

    } catch (InterruptedException | JMSException e) {
      LOG.error("Error in onMessage", e);
    }
  }
}
