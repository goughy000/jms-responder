package com.testingsyndicate.jms.responder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.testingsyndicate.jms.responder.model.config.ConnectionFactoryConfig;
import com.testingsyndicate.jms.responder.model.config.FileConfig;
import com.testingsyndicate.jms.responder.repository.FixedResponseRepository;
import com.testingsyndicate.jms.responder.repository.ResponseRepository;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ResponderServer implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ResponderServer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final ConnectionFactory connectionFactory;
    private final List<String> queueNames;
    private final ResponseRepository repository;
    private final ExecutorService executor;
    private final List<Session> sessions;

    private Connection connection;

    private boolean started = false;

    private ResponderServer(Builder builder) {
        connectionFactory = builder.connectionFactory;
        queueNames = builder.queueNames;
        repository = builder.repository;
        executor = builder.executor;
        sessions = new ArrayList<>();
    }

    public void start() {
        synchronized (this) {
            if (started) {
                throw new IllegalStateException("Already started, Instances of ResponderServer are not reusable");
            }
            started = true;
            try {
                connection = connectionFactory.createConnection();
                connection.start();
                for (String queueName : queueNames) {
                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    sessions.add(session);
                    executor.execute(new MessageHandler(session, queueName, repository));
                }
            } catch (JMSException ex) {
                throw new RuntimeException("Error on startup", ex);
            }
        }
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            executor.shutdownNow();
            for (Session session : sessions) {
                try {
                    session.close();
                } catch (JMSException ex) {
                    // Nom nom nom
                }
            }
            sessions.clear();

            if (null != connection) {
                connection.close();
            }
            connection = null;
        }
    }

    public static ResponderServer fromConfig(InputStream config) throws Exception {
        FileConfig fileConfig = MAPPER.readValue(config, FileConfig.class);

        ConnectionFactoryConfig cfc = fileConfig.getConnectionFactory();
        LOG.info("Initializing {}", cfc);
        Class clazz = Class.forName(cfc.getClazz());
        ConnectionFactory connectionFactory = (ConnectionFactory) clazz.newInstance();
        for (Map.Entry<String, String> prop : cfc.getProperties().entrySet()) {
            LOG.info("Setting {}", prop.getKey());
            BeanUtils.setProperty(connectionFactory, prop.getKey(), prop.getValue());
        }

        return newBuilder()
                .withQueueNames(fileConfig.getQueues())
                .withExecutor(Executors.newFixedThreadPool(fileConfig.getQueues().size()))
                .withRepository(new FixedResponseRepository(fileConfig.getStubs()))
                .withConnectionFactory(connectionFactory)
                .build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private ConnectionFactory connectionFactory;
        private List<String> queueNames;
        private ResponseRepository repository;
        private ExecutorService executor;

        private Builder() {
            queueNames = new ArrayList<>();
        }

        public Builder withConnectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public Builder withQueueName(String queueName) {
            this.queueNames.add(queueName);
            return this;
        }

        public Builder withQueueNames(List<String> queueNames) {
            this.queueNames.addAll(queueNames);
            return this;
        }

        public Builder withRepository(ResponseRepository repository) {
            this.repository = repository;
            return this;
        }

        public Builder withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public ResponderServer build() {
            return new ResponderServer(this);
        }
    }

}
