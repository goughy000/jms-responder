# JMS Responder

## A stubbing utility for JMS Request/Reply

### Introduction

You may have heard of tools such as stubby, wiremock and mockserver for standing up HTTP stubs to "integration test" your HTTP client code.

You may have also seen a few tools which allow you to test some level of integration with JMS, they either capture your messages if you are a producer or can send messages for you to consume.

JMS Responder is a combination of these.  A common pattern in enterprises use message queue servers to implement a request reply pattern.  A client pushes a message to a **request queue** along with a **correlation id** and a **reply destination**.  The client then waits on the **reply destination** queue for a message with the same **correlation id** to appear - this is the reply we have been waiting for!

JMS Responder allows you to stub out the server side of this interaction.  Either through a config file or creating the *stubs* through code.

### Core Components

#### ResponderServer
The *ResponderServer* is responsible for managing the kicking off threads to listen for inbound messages.  It is the main entry point.

#### StubbedResponse
A *StubbedResponse* is a pre-canned Request/Reply pair.  It contains a section on how to match a request, and the body to send in reply (and an optional delay)

#### ResponseRepository
A *ResponseRepository* is responsible for matching an inbound request with a *StubbedResponse*.  The *FixedResponseRepository* is an implementation included which just matches against a fixed list of *StubbedResponses*

#### Matcher
A set of *Matchers* are assigned to each *StubbedResponse* and these are used by the *ResponseRepository* to check if a *StubbedResponse* is the correct one to send back for a given Request, a number of *Matchers* are included

### Matchers

#### AllMatcher
Takes a list of other *Matchers*, and matches if **all** of the *Matchers* match

```yaml
- type: all
  matchers:
    - type: ...
    - type: ...
```

#### AnyMatcher
Takes a list of other *Matchers*, and matches if **any** of the *Matchers* match

```yaml
- type: any
  matchers:
    - type: ...
    - type: ...
```

#### BodyMatcher
Compares the request message body against a fixed value.  Allows for optional trimming before comparison

```yaml
- type: body
  body: body to match
  trim: true
```

#### QueueMatcher
Compares the request queue name against a fixed value

```yaml
- type: queue
  queue: QUEUE.INBOUND
```

### File Config Example
JMS Responder can be kick started from a config file along the lines of this:

```yaml
---
# JMS ConnectionFactory Config
connectionFactory:
  # ConnectionFactory class, will need to be on the class path
  class: org.apache.activemq.ActiveMQConnectionFactory
  # a set of properties to set on the ConnectionFactory
  properties:
    # e.g. this will call .setBrokerURL("vm://...")
    brokerURL: vm://embedded-broker?create=false

# a list of queues to listen on
queues:
  - INBOUND.QUEUE

# a list of StubbedResponses
stubs:
  # Description is just used in logs
  - description: a description
    # List of matchers, see above
    matchers:
      - type: body
        body: this is the body to match
    # The reply body to send
    body: this is the reply body
    # Number of ms to wait before sending the reply
    delay: 1

```

To load this config you can...

```java
InputStream is = new FileInputStream("path/to/config.yaml");
ResponderServer server = ResponderServer.fromConfig(is);
server.start();
```

### Config through code

If you are starting up as part of a Unit Test or Integration test you may prefer to start purely through code.

```java
List<StubbedResponse> responses = ...;
ConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
...

ResponderServer server = ResponderServer.newBuilder()
                .withQueueNames(Arrays.asList("INBOUND.QUEUE"))
                .withExecutor(Executors.newCachedThreadPool())
                .withRepository(new FixedResponseRepository(responses))
                .withConnectionFactory(connectionFactory)
                .build();

server.start();
```