package com.testingsyndicate.jms.responder.model.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.testingsyndicate.jms.responder.matcher.BodyMatcher;
import com.testingsyndicate.jms.responder.model.StubbedResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class FileConfigTest {

    ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper(new YAMLFactory());
    }

    @Test
    public void loadsYaml() throws IOException {
        // given
        InputStream is = fixture("config.yaml");

        // when
        FileConfig actual = mapper.readValue(is, FileConfig.class);

        // then
        assertThat(actual.getQueues()).containsExactly("INBOUND.QUEUE");
        assertThat(actual.getStubs()).hasSize(1);
        StubbedResponse response = actual.getStubs().get(0);
        assertThat(response.getDescription()).isEqualTo("a description");
        assertThat(response.getBody()).isEqualTo("this is the reply body");
        assertThat(response.getDelay()).isEqualTo(1);
        assertThat(response.getMatchers()).hasSize(1);
        BodyMatcher matcher = (BodyMatcher) response.getMatchers().get(0);
        assertThat(matcher.getBody()).isEqualTo("this is the body to match");
    }

    private static InputStream fixture(String path) {
        return FileConfigTest.class.getClassLoader().getResourceAsStream("fixtures/" + path);
    }

}