package org.opendaylight.yangtools.yang.common.jackson.deser;

import static  org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.YangVersion;

public class YangVersionDeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String yangVersion1_1String = "\"1.1\"";
        String yangVersion1String = "1";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(YangVersion.class, new YangVersionDeserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        assertEquals(YangVersion.VERSION_1_1, objectMapper.readValue(yangVersion1_1String, YangVersion.class));
        assertEquals(YangVersion.VERSION_1, objectMapper.readValue(yangVersion1String, YangVersion.class));
        assertNotEquals(YangVersion.VERSION_1_1, objectMapper.readValue(yangVersion1String, YangVersion.class));
        assertNotEquals(YangVersion.VERSION_1, objectMapper.readValue(yangVersion1_1String, YangVersion.class));
    }

}
