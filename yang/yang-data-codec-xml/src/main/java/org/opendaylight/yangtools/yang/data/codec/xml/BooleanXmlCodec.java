/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.DataStringCodec;

final class BooleanXmlCodec extends AbstractXmlCodec<Boolean> {
    BooleanXmlCodec(final DataStringCodec<Boolean> codec) {
        super(codec);
    }

    @Override
    public void serializeToWriter(final XMLStreamWriter writer, final Boolean value) throws XMLStreamException {
        writer.writeCharacters(String.valueOf(value));
    }
}
