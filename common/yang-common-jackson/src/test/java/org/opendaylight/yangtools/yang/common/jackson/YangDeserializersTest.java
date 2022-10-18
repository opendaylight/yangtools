package org.opendaylight.yangtools.yang.common.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.common.jackson.deser.Decimal64Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.EmptyDeserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.RevisionDeserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint16Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint16DeserializerTest;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint32Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint64Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint8Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.YangVersionDeserializer;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class YangDeserializersTest {
    private static final YangDeserializers yangDeserializers = new YangDeserializers();

    @Test
    public void testFindReferenceDeserializer() throws JsonMappingException {
        assertInstanceOf(Decimal64Deserializer.class,
                yangDeserializers.findReferenceDeserializer(ReferenceType.construct(Decimal64.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(EmptyDeserializer.class,
                yangDeserializers.findReferenceDeserializer(ReferenceType.construct(Empty.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(RevisionDeserializer.class,
                yangDeserializers.findReferenceDeserializer(ReferenceType.construct(Revision.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(Uint8Deserializer.class,
                yangDeserializers.findReferenceDeserializer(ReferenceType.construct(Uint8.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(Uint16Deserializer.class,
                yangDeserializers.findReferenceDeserializer(ReferenceType.construct(Uint16.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(Uint32Deserializer.class,
                yangDeserializers.findReferenceDeserializer(ReferenceType.construct(Uint32.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(Uint64Deserializer.class,
                yangDeserializers.findReferenceDeserializer(ReferenceType.construct(Uint64.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(YangVersionDeserializer.class,
                yangDeserializers.findReferenceDeserializer(ReferenceType.construct(YangVersion.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
    }
}
