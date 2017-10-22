# JMS Responder

[![Maven Central](https://img.shields.io/maven-central/v/com.testingsyndicate/jms-responder.svg)](https://mvnrepository.com/artifact/com.testingsyndicate/jms-responder)

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

#### XmlMatcher
Compares the request message body against a fixed value of XML.  Strips out whitespace and normalizes namespaces.

Requests don't have to be xml, but the expected body **must** be valid xml

```yaml
- type: xml
  body: |-
    <xml>
      <value>valid xml</value>
    </xml>
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

### Maven plugin
For integration testing, you can boot JMS Responder as part of the maven lifecycle

Here is an example of starting ActiveMQ and JMS Responder for the integration test phase, and tearing it all down afterwards

#### src/test/resources/responder.yaml

```yaml
---
connectionFactory:
  class: org.apache.activemq.ActiveMQConnectionFactory
  properties:
    brokerURL: tcp://localhost:61616

queues:
  - INBOUND.QUEUE

stubs:
  - #...
```

#### pom.xml
```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.testingsyndicate</groupId>
  <artifactId>plugin-test</artifactId>
  <version>0.1-SNAPSHOT</version>
  <packaging>jar</packaging>

  <name>Example</name>

  <properties>
    <activemq.version>5.15.1</activemq.version>
    <activemq-plugin.version>5.7.0</activemq-plugin.version>
    <responder.version>1.0.1</responder.version>
  </properties>

  <build>
    <plugins>
      <!-- Start & Stop ActiveMQ -->
      <plugin>
        <groupId>org.apache.activemq.tooling</groupId>
        <artifactId>maven-activemq-plugin</artifactId>
        <version>${activemq-plugin.version}</version>
        <executions>
          <execution>
            <goals>
              <goal>run</goal>
            </goals>
            <configuration>
              <fork>true</fork>
            </configuration>
          </execution>
        </executions>
      </plugin>

      <!-- Start & Stop JMS Responder -->
      <plugin>
        <groupId>com.testingsyndicate</groupId>
        <artifactId>jms-responder-maven-plugin</artifactId>
        <version>${responder.version}</version>
        <executions>
          <execution>
            <goals>
              <goal>start</goal>
              <goal>stop</goal>
            </goals>
            <configuration>
              <!-- this is the default value, but you can change it -->
              <configFile>src/test/resources/responder.yaml</configFile>
            </configuration>
          </execution>
        </executions>
        <dependencies>
          <!-- You need to include the correct dependencies for the ConnectionFactory you are going to use -->
          <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-client</artifactId>
            <version>${activemq.version}</version>
          </dependency>
          <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-broker</artifactId>
            <version>${activemq.version}</version>
          </dependency>
        </dependencies>
      </plugin>
      <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-antrun-plugin</artifactId>
          <configuration>
              <tasks>
                  <sleep seconds="20" />
              </tasks>
          </configuration>
          <executions>
              <execution>
                  <id>sleep-for-a-while</id>
                  <phase>pre-integration-test</phase>
                  <goals>
                      <goal>run</goal>
                  </goals>
              </execution>
          </executions>
      </plugin>
    </plugins>
  </build>

</project>
```

```
mvn clean verify
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building Example 0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ plugin-test ---
[INFO] Deleting /Users/test/dummy-project/target
[INFO]
[INFO] --- maven-activemq-plugin:5.7.0:run (default) @ plugin-test ---
[INFO] Loading broker configUri: broker:(tcp://localhost:61616)?useJmx=false&persistent=false
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ plugin-test ---
[INFO] Using Persistence Adapter: MemoryPersistenceAdapter
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] Copying 2 resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ plugin-test ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 1 source file to /Users/test/dummy-project/target/classes
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ plugin-test ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/test/dummy-project/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ plugin-test ---
[INFO] No sources to compile
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ plugin-test ---
[INFO] No tests to run.
[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ plugin-test ---
[INFO] Building jar: /Users/test/dummy-project/target/plugin-test-0.1-SNAPSHOT.jar
[INFO]
[INFO] --- jms-responder-maven-plugin:1.0.0-SNAPSHOT:start (default) @ plugin-test ---
[INFO] Loading responder config /Users/test/dummy-project/src/main/resources/responder.yaml
[INFO] Initializing class org.apache.activemq.ActiveMQConnectionFactory
[INFO] Setting brokerURL
[INFO] Starting responder
[INFO] Apache ActiveMQ 5.7.0 (localhost) is starting
[INFO] Listening for connections at: tcp://localhost:61616
[INFO] Connector tcp://localhost:61616 Started
[INFO] Apache ActiveMQ 5.7.0 (localhost) started
[INFO] For help or more information please see: http://activemq.apache.org
[INFO]
[INFO] --- maven-antrun-plugin:1.3:run (sleep-for-a-while) @ plugin-test ---
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Executing tasks
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Waiting for message on queue://INBOUND.QUEUE for 2000ms
[INFO] Executed tasks
[INFO]
[INFO] --- jms-responder-maven-plugin:1.0.0-SNAPSHOT:stop (default) @ plugin-test ---
[INFO] Stopping responder
[INFO] Shutting down
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 36.150 s
[INFO] Finished at: 2017-10-22T08:20:01+01:00
[INFO] Final Memory: 30M/318M
[INFO] ------------------------------------------------------------------------
[INFO] Apache ActiveMQ 5.7.0 (localhost) is shutting down
```
