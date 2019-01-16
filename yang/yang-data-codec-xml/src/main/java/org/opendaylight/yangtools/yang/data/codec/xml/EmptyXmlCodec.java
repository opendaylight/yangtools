/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.Empty;

final class EmptyXmlCodec implements XmlCodec<Empty> {

    static final EmptyXmlCodec INSTANCE = new EmptyXmlCodec();

    private EmptyXmlCodec() {

    }

    @Override
    public Class<Empty> getDataType() {
        return Empty.class;
    }

    @Override
    public Empty parseValue(final NamespaceContext ctx, final String str) {
        return Empty.getInstance();
    }

    @Override
    public void writeValue(final XMLStreamWriter ctx, final Empty value) throws XMLStreamException {
        requireNonNull(value);
    }
}
