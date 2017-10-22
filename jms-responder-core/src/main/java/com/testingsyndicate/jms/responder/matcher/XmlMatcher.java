package com.testingsyndicate.jms.responder.matcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testingsyndicate.jms.responder.model.RequestInfo;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

public class XmlMatcher implements Matcher {

    private static final DOMImplementationLS DOM_IMPL;

    static {
        DOMImplementationRegistry registry;
        try {
            registry = DOMImplementationRegistry.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DOM_IMPL = (DOMImplementationLS) registry.getDOMImplementation("LS");
    }

    private final String body;
    private final String prettyBody;

    @JsonCreator
    public XmlMatcher(@JsonProperty("body") String body) {
        this.body = body;

        String prettyBody = prettyPrint(body);
        if (null == prettyBody) {
            throw new InvalidMatcherException("Invalid XML Body " + body);
        }

        this.prettyBody = prettyBody;
    }

    @Override
    public boolean matches(RequestInfo requestInfo) {
        String requestBody = requestInfo.getBody();
        return Objects.equals(body, requestBody)
                || Objects.equals(prettyBody, prettyPrint(requestBody));
    }

    private static Document loadDocument(String xml) {
        try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {
            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(is);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String prettyPrint(String ugly) {
        Document document = loadDocument(ugly);

        if (null == document)
            return null;

        LSSerializer serializer = DOM_IMPL.createLSSerializer();
        serializer.getDomConfig().setParameter("format-pretty-print", true);
        return serializer.writeToString(document);
    }
}
