package org.opendaylight.yangtools.yang.common.jackson.ser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Empty;

public class EmptySerializerTest {

    @Test
    public void testSerialize(){
        Empty empty = Empty.value();
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Empty.class, new EmptySerializer());
        objectMapper.registerModule(simpleModule);

        try {
            assertEquals("\"[null]\"", objectMapper.writeValueAsString(empty));
        } catch (JsonProcessingException e) {
            assertNotNull("objectMapper.writeValueAsString() throws JsonProcessingException on an Empty object",null);
        }

    }

}
