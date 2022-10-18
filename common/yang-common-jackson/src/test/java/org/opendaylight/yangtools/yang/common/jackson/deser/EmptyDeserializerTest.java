package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Empty;

public class EmptyDeserializerTest {

    @Test
    public void testDeserialize() {
        String emptyString = "[null]";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(Empty.class, new EmptyDeserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        try {
            assertEquals("empty", objectMapper.readValue(emptyString, Empty.class).toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
