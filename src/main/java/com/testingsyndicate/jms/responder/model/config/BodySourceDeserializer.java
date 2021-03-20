package com.testingsyndicate.jms.responder.model.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.testingsyndicate.jms.responder.model.BodySource;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class BodySourceDeserializer extends JsonDeserializer<BodySource> {

    private static final String FILE_KEY = "@@";

    private final String basePath;

    public BodySourceDeserializer(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public BodySource deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String content = jsonParser.getText();
        if (null != content && content.startsWith(FILE_KEY)) {
            String path = Paths.get(basePath, content.substring(FILE_KEY.length())).toString();
            try (InputStream is = new FileInputStream(path);
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                }
                content = os.toString("UTF-8");
            }
        }

        return new BodySource(content);
    }
}
