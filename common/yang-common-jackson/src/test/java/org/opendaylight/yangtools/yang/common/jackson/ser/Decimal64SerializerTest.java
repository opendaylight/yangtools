package org.opendaylight.yangtools.yang.common.jackson.ser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.DisplayNameGenerator.Simple;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;

public class Decimal64SerializerTest {

    @Test
    public void testSerialize(){
        Decimal64 correctDecimal64 = Decimal64.valueOf("125");
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Decimal64.class, new Decimal64Serializer());
        mapper.registerModule(module);

        try {
            assertEquals("\"125.0\"", mapper.writeValueAsString(correctDecimal64));
        } catch (JsonProcessingException e) {
            assertNotNull(null);
        }
        assertThrows(java.lang.NumberFormatException.class, () -> Decimal64.valueOf("auto"));

    }
}
