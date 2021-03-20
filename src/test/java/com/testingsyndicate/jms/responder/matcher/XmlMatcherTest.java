package com.testingsyndicate.jms.responder.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import com.testingsyndicate.jms.responder.model.BodySource;
import com.testingsyndicate.jms.responder.model.RequestInfo;
import org.junit.jupiter.api.Test;

class XmlMatcherTest {

  @Test
  void matchesBinaryEqualXml() {
    // given
    XmlMatcher sut = xmlMatcher("<xml></xml>");

    // when
    boolean actual = sut.matches(withBody("<xml></xml>"));

    // then
    assertThat(actual).isTrue();
  }

  @Test
  void doesntMatchUnequalXml() {
    // given
    XmlMatcher sut = xmlMatcher("<xml>1</xml>");

    // when
    boolean actual = sut.matches(withBody("<xml>2</xml>"));

    // then
    assertThat(actual).isFalse();
  }

  @Test
  void matchesEquivalentXml() {
    // given
    XmlMatcher sut = xmlMatcher("<xml><one /><two /></xml>");

    // when
    boolean actual = sut.matches(withBody("<xml> <one />          <two />\n</xml>"));

    // then
    assertThat(actual).isTrue();
  }

  @Test
  void cannotInitWithInvalidXml() {
    // given
    String invalidXml = "wibble";

    // when
    try {
      xmlMatcher(invalidXml);
    } catch (InvalidMatcherException actual) {
      // then
      assertThat(actual).hasMessage("Invalid XML Body wibble");
    }
  }

  @Test
  void falseWhenInvalidXml() {
    // given
    XmlMatcher sut = xmlMatcher("<xml></xml>");
    String invalidXml = "wibble";

    // when
    boolean actual = sut.matches(withBody(invalidXml));

    // then
    assertThat(actual).isFalse();
  }

  private static XmlMatcher xmlMatcher(String body) {
    return new XmlMatcher(new BodySource(body));
  }

  private static RequestInfo withBody(String body) {
    return RequestInfo.newBuilder().withBody(body).build();
  }
}
