/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated(since = "13.0.3", forRemoval = true)
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
    public Object parseValue(final NamespaceContext ctx, final String str) {
        LOG.warn("Call of the deserializeString method on null codec. No operation performed.");
        return null;
    }

    @Override
    public void writeValue(final XMLStreamWriter ctx, final Object value) throws XMLStreamException {
        // NOOP since codec is unkwown.
        LOG.warn("Call of the serializeToWriter method on null codec. No operation performed.");
    }
}
