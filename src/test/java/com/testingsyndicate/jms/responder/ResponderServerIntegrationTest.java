package com.testingsyndicate.jms.responder;

import static org.assertj.core.api.Assertions.assertThat;

import com.testingsyndicate.jms.responder.model.config.FileConfig;
import java.io.File;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.jms.*;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResponderServerIntegrationTest {

  private static ConnectionFactory connectionFactory;
  private Connection connection;
  private Session session;
  private MessageProducer producer;
  private MessageConsumer consumer;
  private Destination sendTo;
  private Destination replyTo;

  private ResponderServer sut;

  @BeforeAll
  static void beforeAll() throws Exception {
    Configuration inMemoryMQConfiguration =
        new ConfigurationImpl()
            .setPersistenceEnabled(false)
            .setJournalDirectory("target/data/journal")
            .setSecurityEnabled(false)
            .addAcceptorConfiguration("invm", "vm://0");
    ActiveMQServer server = ActiveMQServers.newActiveMQServer(inMemoryMQConfiguration);
    server.start();

    connectionFactory = new ActiveMQConnectionFactory("vm://0");
  }

  @BeforeEach
  void beforeEach() throws Exception {
    connection = connectionFactory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    sendTo = session.createQueue("INBOUND.QUEUE");
    replyTo = session.createQueue("REPLY.QUEUE");

    producer = session.createProducer(sendTo);
    consumer = session.createConsumer(replyTo);

    File config = fixture("integration.yaml");
    sut = ResponderServer.fromConfig(FileConfig.fromFile(config));
    sut.start();
  }

  @AfterEach
  void afterEach() throws Exception {
    if (null != consumer) {
      consumer.close();
    }

    if (null != producer) {
      producer.close();
    }

    if (null != session) {
      session.close();
    }

    if (null != connection) {
      connection.close();
    }

    sut.close();
  }

  @Test
  void simpleBodyMatch() throws Exception {
    // given
    String correlationId = UUID.randomUUID().toString();

    // when
    TextMessage actual = exchangeMessage(correlationId, "Hello");

    // then
    assertThat(actual.getText()).isEqualTo("Hello back to you!");
    assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
  }

  @Test
  void noCorrelationId() throws Exception {
    // given

    // when
    TextMessage actual = exchangeMessage(null, "Hello");

    // then
    assertThat(actual.getText()).isEqualTo("Hello back to you!");
    assertThat(actual.getJMSCorrelationID()).isNotNull();
  }

  @Test
  void missesFirstStubToMatchSecond() throws Exception {
    // given
    String correlationId = UUID.randomUUID().toString();

    // when
    TextMessage actual = exchangeMessage(correlationId, "wibble");

    // then
    assertThat(actual.getText()).isEqualTo("wobble");
    assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
  }

  @Test
  void matchesOnBodyAndQueue() throws Exception {
    // given
    String correlationId = UUID.randomUUID().toString();

    // when
    TextMessage actual = exchangeMessage(correlationId, "wobble");

    // then
    assertThat(actual.getText()).isEqualTo("queue matched");
    assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
  }

  @Test
  void matchesOnBodyXml() throws Exception {
    // given
    String correlationId = UUID.randomUUID().toString();

    // when
    TextMessage actual = exchangeMessage(correlationId, "<xml> <wibble />  </xml>");

    // then
    assertThat(actual.getText()).isEqualTo("<xml><response /></xml>");
    assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
  }

  @Test
  void fallsBackToDefault() throws Exception {
    // given
    String correlationId = UUID.randomUUID().toString();

    // when
    TextMessage actual = exchangeMessage(correlationId, "any old rubbish");

    // then
    assertThat(actual.getText()).isEqualTo("the default");
    assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
  }

  private TextMessage exchangeMessage(String correlationId, String text) throws JMSException {
    // Send
    TextMessage request = session.createTextMessage(text);
    request.setJMSDestination(sendTo);
    request.setJMSCorrelationID(correlationId);
    request.setJMSReplyTo(replyTo);
    producer.send(request);

    // Receive
    return (TextMessage) consumer.receive(5000);
  }

  private static File fixture(String path) {
    try {
      return new File(
          ResponderServerIntegrationTest.class
              .getClassLoader()
              .getResource("fixtures/" + path)
              .toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Unable to load test fixture", e);
    }
  }
}
