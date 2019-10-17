package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;
import java.math.BigInteger;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

final class Uint8Deserializer extends StdScalarDeserializer<Uint8> {
    private static final long serialVersionUID = 1L;

    Uint8Deserializer(final Class<?> vc) {
        super(Uint8.class);
    }

    @Override
    public Uint8 deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException         {
        switch (p.getCurrentTokenId()) {
        case JsonTokenId.ID_NUMBER_INT:
            switch (p.getNumberType()) {
            case INT:
                return Uint8.valueOf(p.getIntValue());
            case LONG:
                return Uint8.valueOf(p.getLongValue());
            case BIG_INTEGER:
                return Uint64.valueOf(p.getBigIntegerValue()).toUint8();
            }
            break;
        case JsonTokenId.ID_NUMBER_FLOAT:
            if (!ctxt.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT)) {
                _failDoubleToIntCoercion(p, ctxt, "org.opendaylight.yangtools.yang.common.Uint8");
            }
            return Uint64.valueOf(p.getDecimalValue().toBigInteger()).toUint8();
        case JsonTokenId.ID_START_ARRAY:
            return _deserializeFromArray(p, ctxt);
        case JsonTokenId.ID_STRING: // let's do implicit re-parse
            String text = p.getText().trim();
            // note: no need to call `coerce` as this is never primitive
            if (_isEmptyOrTextualNull(text)) {
                _verifyNullForScalarCoercion(ctxt, text);
                return getNullValue(ctxt);
            }
            _verifyStringForScalarCoercion(ctxt, text);
            try {
                return new BigInteger(text);
            } catch (IllegalArgumentException iae) { }
            return (BigInteger) ctxt.handleWeirdStringValue(_valueClass, text,
                    "not a valid representation");
        }
        // String is ok too, can easily convert; otherwise, no can do:
        return (BigInteger) ctxt.handleUnexpectedToken(_valueClass, p);
    }
}
}
