package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint32;

public class Uint32DeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String uint32String = "156";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(Uint32.class, new Uint32Deserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        assertEquals(156, objectMapper.readValue(uint32String, Uint32.class).intValue());
    }

}
