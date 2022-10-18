package org.opendaylight.yangtools.yang.common.jackson.ser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint64;

public class Uint64SerializerTest {

    @Test
    public void testSerialize(){
        Uint64 uint64 = Uint64.saturatedOf(168);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Uint64.class, new Uint64Serializer());
        objectMapper.registerModule(simpleModule);

        try {
            assertEquals("\"168\"", objectMapper.writeValueAsString(uint64));
        } catch (JsonProcessingException e) {
            assertNotNull("objectMapper.writeValueAsString() throws JsonProcessingException on a Uint64 object", null);
        }
    }

}
