package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;

public class RevisionDeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String revisionString = "2022-10-18";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(Revision.class, new RevisionDeserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        assertEquals("2022-10-18", objectMapper.readValue(revisionString, Revision.class));
    }


}
