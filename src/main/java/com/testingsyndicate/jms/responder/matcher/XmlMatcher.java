package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testingsyndicate.jms.responder.model.BodySource;
import com.testingsyndicate.jms.responder.model.Request;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public final class XmlMatcher implements Matcher {

  private static final DOMImplementationLS DOM;
  private static final DocumentBuilderFactory DBF;

  static {
    DOMImplementationRegistry registry;
    try {
      registry = DOMImplementationRegistry.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    DOM = (DOMImplementationLS) registry.getDOMImplementation("LS");
    DBF = DocumentBuilderFactory.newInstance();
  }

  private final String body;
  private final String prettyBody;

  @JsonCreator
  public XmlMatcher(@JsonProperty("body") BodySource source) {
    this.body = source.getBody();

    String prettyBody = prettyPrint(body);
    if (null == prettyBody) {
      throw new InvalidMatcherException("Invalid XML Body " + body);
    }

    this.prettyBody = prettyBody;
  }

  @Override
  public boolean matches(Request request) {
    String requestBody = request.getBody();
    return Objects.equals(body, requestBody)
        || Objects.equals(prettyBody, prettyPrint(requestBody));
  }

  private static Document loadDocument(String xml) {
    try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {
      return DBF.newDocumentBuilder().parse(is);
    } catch (Exception ex) {
      return null;
    }
  }

  private static String prettyPrint(String ugly) {
    Document document = loadDocument(ugly);

    if (null == document) return null;

    LSSerializer serializer = DOM.createLSSerializer();
    serializer.getDomConfig().setParameter("format-pretty-print", true);
    return serializer.writeToString(document);
  }
}
