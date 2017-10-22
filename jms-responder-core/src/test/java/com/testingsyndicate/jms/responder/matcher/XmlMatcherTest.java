package com.testingsyndicate.jms.responder.matcher;

import com.testingsyndicate.jms.responder.model.RequestInfo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlMatcherTest {

    @Test
    public void matchesBinaryEqualXml() {
        // given
        XmlMatcher sut = new XmlMatcher("<xml></xml>");

        // when
        boolean actual = sut.matches(withBody("<xml></xml>"));

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void doesntMatchUnequalXml() {
        // given
        XmlMatcher sut = new XmlMatcher("<xml>1</xml>");

        // when
        boolean actual = sut.matches(withBody("<xml>2</xml>"));

        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void matchesEquivalentXml() {
        // given
        XmlMatcher sut = new XmlMatcher("<xml><one /><two /></xml>");

        // when
        boolean actual = sut.matches(withBody("<xml> <one />          <two />\n</xml>"));

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void cannotInitWithInvalidXml() {
        // given
        String invalidXml = "wibble";

        // when
        try {
            new XmlMatcher(invalidXml);
        } catch (InvalidMatcherException actual) {
            // then
            assertThat(actual).hasMessage("Invalid XML Body wibble");
        }
    }

    @Test
    public void falseWhenInvalidXml() {
        // given
        XmlMatcher sut = new XmlMatcher("<xml></xml>");
        String invalidXml = "wibble";

        // when
        boolean actual = sut.matches(withBody(invalidXml));

        // then
        assertThat(actual).isFalse();
    }

    private static RequestInfo withBody(String body) {
        return RequestInfo.newBuilder()
                .withBody(body)
                .build();
    }
}
