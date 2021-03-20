package com.testingsyndicate.jms.responder;

import static org.mockito.Mockito.*;

import com.testingsyndicate.jms.responder.ResponderServer.Builder;
import java.util.concurrent.ExecutorService;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import org.junit.Before;
import org.junit.Test;

public class ResponderServerTest {

  private ConnectionFactory mockConnectionFactory;
  private Connection mockConnection;
  private Session mockSession;
  private ExecutorService mockExecutor;

  private Builder builder;
  private ResponderServer sut;

  @Before
  public void before() throws Exception {
    mockConnectionFactory = mock(ConnectionFactory.class);
    mockExecutor = mock(ExecutorService.class);
    mockConnection = mock(Connection.class);
    mockSession = mock(Session.class);

    when(mockConnectionFactory.createConnection()).thenReturn(mockConnection);
    when(mockConnection.createSession(anyBoolean(), anyInt())).thenReturn(mockSession);

    builder =
        ResponderServer.newBuilder()
            .withThreads(4)
            .withConnectionFactory(mockConnectionFactory)
            .withExecutor(mockExecutor)
            .withQueueName("bla");

    sut = builder.build();
  }

  @Test
  public void submitsOneTaskWhenZeroThreads() {
    // given
    builder.withThreads(0);
    sut = builder.build();

    // when
    sut.start();

    // then
    verify(mockExecutor, times(1)).execute(any(Runnable.class));
  }

  @Test
  public void submitsMultipleTasksWhenMultipleThreads() {
    // given
    builder.withThreads(10);
    sut = builder.build();

    // when
    sut.start();

    // then
    verify(mockExecutor, times(10)).execute(any(Runnable.class));
  }
}
