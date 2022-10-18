package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint64;

public class Uint64DeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String uint64String = "156";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(Uint64.class, new Uint64Deserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        assertEquals(156, objectMapper.readValue(uint64String, Uint64.class).intValue());
    }

}
