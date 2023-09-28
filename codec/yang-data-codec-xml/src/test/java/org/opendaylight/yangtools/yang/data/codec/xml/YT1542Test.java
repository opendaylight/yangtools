/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@ExtendWith(MockitoExtension.class)
class YT1542Test {
    @Mock
    private XMLStreamWriter writer;
    @Mock
    private NamespaceContext context;

    @Test
    void writeInstanceIdentifierReportsIOException() {
        final var codec = XmlCodecFactory.create(YangParserTestUtils.parseYang()).instanceIdentifierCodec();

        doReturn(context).when(writer).getNamespaceContext();
        final var ex = assertThrows(XMLStreamException.class, () -> codec.writeValue(writer,
            YangInstanceIdentifier.of(QName.create("foo", "bar"))));
        assertEquals("Failed to encode instance-identifier", ex.getMessage());
        final var cause = assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals("Invalid input /(foo)bar: schema for argument (foo)bar (after \"\") not found",
            cause.getMessage());
    }
}
