package com.testingsyndicate.jms.responder;

import com.testingsyndicate.jms.responder.model.config.FileConfigTest;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.Rule;
import org.junit.Test;

import javax.jms.*;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponderServerTest {

    @Rule
    public EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker();

    @Test
    public void buildsResponderServer() throws Exception {
        // start up
        ConnectionFactory connectionFactory = broker.createConnectionFactory();
        InputStream config = fixture("integration.yaml");
        ResponderServer server = ResponderServer.fromConfig(config);
        server.start();


        String correlationId = UUID.randomUUID().toString();
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

            // Send message
            TextMessage message = session.createTextMessage("Hello");
            message.setJMSDestination(sendTo);
            message.setJMSCorrelationID(correlationId);
            message.setJMSReplyTo(replyTo);

            producer = session.createProducer(sendTo);
            producer.send(message);

            // Read reply
            consumer = session.createConsumer(replyTo);
            TextMessage reply = (TextMessage) consumer.receive(5000);
            assertThat(reply.getText()).isEqualTo("Hello back to you!");
            assertThat(reply.getJMSCorrelationID()).isEqualTo(correlationId);

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

}
