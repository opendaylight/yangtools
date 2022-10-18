package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;

public class Decimal64DeserializerTest {

    @Test
    public void testDeserialize(){
        String correctDecimal64String = "\"255.0\"";
        String correctDecimal64String2 = "255.0";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(Decimal64.class, new Decimal64Deserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        try {
            assertEquals("255.0", objectMapper.readValue(correctDecimal64String, Decimal64.class).toString());
            assertEquals("255.0", objectMapper.readValue(correctDecimal64String2, Decimal64.class).toString());
        } catch (JsonProcessingException e) {
            assertNull("objectMapper.readValue() throws JsonProcessingException while deserialising to Decimal64",
                    null);
        }
    }

}
