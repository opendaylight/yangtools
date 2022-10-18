package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint8;

public class Uint8DeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String uint8String = "156";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(Uint8.class, new Uint8Deserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        assertEquals(156, objectMapper.readValue(uint8String, Uint8.class).intValue());
    }

}
