package com.testingsyndicate.jms.responder;

import com.testingsyndicate.jms.responder.model.config.FileConfigTest;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.Rule;
import org.junit.Test;

import javax.jms.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponderServerIntegrationTest {

    @Rule
    public EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker();

    @Test
    public void integrationTest() throws Exception {
        // start up
        ConnectionFactory connectionFactory = broker.createConnectionFactory();
        InputStream config = fixture("integration.yaml");
        ResponderServer server = ResponderServer.fromConfig(config);
        server.start();

        // Open up
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer consumer = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination sendTo = session.createQueue("INBOUND.QUEUE");
            Destination replyTo = session.createQueue("REPLY.QUEUE");

            List<TestData> data = Arrays.asList(
                    new TestData("Hello", "Hello back to you!"),
                    new TestData("wibble", "wobble"),
                    new TestData("wobble", "queue matched")
            );

            producer = session.createProducer(sendTo);
            consumer = session.createConsumer(replyTo);

            for (TestData td : data) {

                String correlationId = UUID.randomUUID().toString();

                // Send message
                TextMessage message = session.createTextMessage(td.request);
                message.setJMSDestination(sendTo);
                message.setJMSCorrelationID(correlationId);
                message.setJMSReplyTo(replyTo);
                producer.send(message);

                // Read reply
                TextMessage reply = (TextMessage) consumer.receive(5000);
                assertThat(reply.getText()).isEqualTo(td.response);
                assertThat(reply.getJMSCorrelationID()).isEqualTo(correlationId);
            }

        } finally {
            server.close();

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
    }

    private static InputStream fixture(String path) {
        return FileConfigTest.class.getClassLoader().getResourceAsStream("fixtures/" + path);
    }

    private static final class TestData {
        private String request;
        private String response;

        public TestData(String request, String response) {
            this.request = request;
            this.response = response;
        }
    }

}
