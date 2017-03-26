package org.opendaylight.yangtools.yang.data.codec.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class NullXmlCodec implements XmlCodec<Object> {
    static final NullXmlCodec INSTANCE = new NullXmlCodec();
    private static final Logger LOG = LoggerFactory.getLogger(NullXmlCodec.class);

    private NullXmlCodec() {

    }

    @Override
    public Class<Object> getDataType() {
        return Object.class;
    }

    @Override
    public void writeValue(final XMLStreamWriter writer, final Object value) throws XMLStreamException {
        // NOOP since codec is unkwown.
        LOG.warn("Call of the serializeToWriter method on null codec. No operation performed.");
    }

    @Override
    public Object parseValue(final NamespaceContext namespaceContext, final String value) {
        LOG.warn("Call of the deserializeString method on null codec. No operation performed.");
        return null;
    }

}
