package com.testingsyndicate.jms.responder;

import com.testingsyndicate.jms.responder.model.config.FileConfigTest;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.*;

import javax.jms.*;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponderServerIntegrationTest {

    private static Connection connection;
    private static Session session;
    private static MessageProducer producer;
    private static MessageConsumer consumer;
    private static Destination sendTo;
    private static Destination replyTo;

    private ResponderServer sut;

    @ClassRule
    public static EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker();

    @BeforeClass
    public static void setUp() throws JMSException {
        ConnectionFactory connectionFactory = broker.createConnectionFactory();
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        sendTo = session.createQueue("INBOUND.QUEUE");
        replyTo = session.createQueue("REPLY.QUEUE");

        producer = session.createProducer(sendTo);
        consumer = session.createConsumer(replyTo);
    }

    @Before
    public void before() throws Exception {
        InputStream config = fixture("integration.yaml");
        sut = ResponderServer.fromConfig(config);
        sut.start();
    }

    @After
    public void after() throws Exception {
        sut.close();
    }

    @AfterClass
    public static void tearDown() throws JMSException {
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
    }

    @Test
    public void simpleBodyMatch() throws Exception {
        // given
        String correlationId = UUID.randomUUID().toString();

        // when
        TextMessage actual = exchangeMessage(correlationId, "Hello");

        // then
        assertThat(actual.getText()).isEqualTo("Hello back to you!");
        assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
    }

    @Test
    public void missesFirstStubToMatchSecond() throws Exception {
        // given
        String correlationId = UUID.randomUUID().toString();

        // when
        TextMessage actual = exchangeMessage(correlationId, "wibble");

        // then
        assertThat(actual.getText()).isEqualTo("wobble");
        assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
    }

    @Test
    public void matchesOnBodyAndQueue() throws Exception {
        // given
        String correlationId = UUID.randomUUID().toString();

        // when
        TextMessage actual = exchangeMessage(correlationId, "wobble");

        // then
        assertThat(actual.getText()).isEqualTo("queue matched");
        assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
    }

    @Test
    public void fallsBackToDefault() throws Exception {
        // given
        String correlationId = UUID.randomUUID().toString();

        // when
        TextMessage actual = exchangeMessage(correlationId, "any old rubbish");

        // then
        assertThat(actual.getText()).isEqualTo("the default");
        assertThat(actual.getJMSCorrelationID()).isEqualTo(correlationId);
    }

    private static TextMessage exchangeMessage(String correlationId, String text) throws JMSException {
        // Send
        TextMessage request = session.createTextMessage(text);
        request.setJMSDestination(sendTo);
        request.setJMSCorrelationID(correlationId);
        request.setJMSReplyTo(replyTo);
        producer.send(request);

        // Receive
        return (TextMessage) consumer.receive(5000);
    }

    private static InputStream fixture(String path) {
        return FileConfigTest.class.getClassLoader().getResourceAsStream("fixtures/" + path);
    }

}
