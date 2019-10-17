package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.opendaylight.yangtools.yang.common.Uint8;

final class UIint8Serializer extends ToStringSerializer {
    private static final long serialVersionUID = 1L;

    UIint8Serializer() {
        super(Uint8.class);
    }
}
