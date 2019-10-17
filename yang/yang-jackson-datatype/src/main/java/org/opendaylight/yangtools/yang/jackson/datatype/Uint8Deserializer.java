package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.Uint8;

final class Uint8Deserializer extends StdScalarDeserializer<Uint8> {
    private static final long serialVersionUID = 1L;

    static final Uint8Deserializer INSTANCE = new Uint8Deserializer();

    private Uint8Deserializer() {
        super(Uint8.class);
    }

    @Override
    public Uint8 deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return Uint8.valueOf(p.getShortValue());
        }


        // TODO Auto-generated method stub
        return null;
    }
}
