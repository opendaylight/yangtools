package org.opendaylight.yangtools.yang.common.jackson;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.fasterxml.jackson.databind.type.ReferenceType;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.common.jackson.ser.Decimal64Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.EmptySerializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.RevisionSerializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint16Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint32Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint64Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint8Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.YangVersionSerializer;

public class YangSerializersTest {
    private static final YangSerializers yangSerializers = new YangSerializers();

    @Test
    public void testFindReferenceSerialiser(){
        assertInstanceOf(Decimal64Serializer.class, yangSerializers.findReferenceSerializer(null,
                ReferenceType.construct(Decimal64.class, null, null, null, null),
                null, null, null ));
        assertInstanceOf(EmptySerializer.class, yangSerializers.findReferenceSerializer(null,
                ReferenceType.construct(Empty.class, null, null, null, null),
                null, null, null ));
        assertInstanceOf(RevisionSerializer.class, yangSerializers.findReferenceSerializer(null,
                ReferenceType.construct(Revision.class, null, null, null, null),
                null, null, null ));
        assertInstanceOf(Uint8Serializer.class, yangSerializers.findReferenceSerializer(null,
                ReferenceType.construct(Uint8.class, null, null, null, null),
                null, null, null ));
        assertInstanceOf(Uint16Serializer.class, yangSerializers.findReferenceSerializer(null,
                ReferenceType.construct(Uint16.class, null, null, null, null),
                null, null, null ));
        assertInstanceOf(Uint32Serializer.class, yangSerializers.findReferenceSerializer(null,
                ReferenceType.construct(Uint32.class, null, null, null, null),
                null, null, null ));
        assertInstanceOf(Uint64Serializer.class, yangSerializers.findReferenceSerializer(null,
                ReferenceType.construct(Uint64.class, null, null, null, null),
                null, null, null ));
        assertInstanceOf(YangVersionSerializer.class, yangSerializers.findReferenceSerializer(null,
                ReferenceType.construct(YangVersion.class, null, null, null, null),
                null, null, null ));
    }
}
