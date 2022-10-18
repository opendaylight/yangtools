package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint16;

public class Uint16DeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String uint16String = "156";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(Uint16.class, new Uint16Deserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        assertEquals(156, objectMapper.readValue(uint16String, Uint16.class).intValue());
    }

}
