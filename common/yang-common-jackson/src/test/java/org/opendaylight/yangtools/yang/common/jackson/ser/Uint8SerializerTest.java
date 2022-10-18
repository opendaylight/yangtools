package org.opendaylight.yangtools.yang.common.jackson.ser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint8;

public class Uint8SerializerTest {

    @Test
    public void testSerialize(){
        Uint8 uint8 = Uint8.saturatedOf(168);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Uint8.class, new Uint8Serializer());
        objectMapper.registerModule(simpleModule);

        try {
            assertEquals("168", objectMapper.writeValueAsString(uint8));
        } catch (JsonProcessingException e) {
            assertNotNull("objectMapper.writeValueAsString() throws JsonProcessingException on a Uint8 object", null);
        }
    }

}
