/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

final class EmptyXmlCodec implements XmlCodec<Void> {

    static final EmptyXmlCodec INSTANCE = new EmptyXmlCodec();

    private EmptyXmlCodec() {

    }

    @Override
    public Class<Void> getDataType() {
        return Void.class;
    }

    @Override
    public Void parseValue(final NamespaceContext namespaceContext, final String value) {
        return null;
    }

    @Override
    public void writeValue(final XMLStreamWriter writer, final Void value) throws XMLStreamException {
        writer.writeCharacters("");
    }
}
