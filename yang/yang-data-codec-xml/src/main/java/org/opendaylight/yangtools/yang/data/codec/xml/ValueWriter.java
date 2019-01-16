/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * A minimal facade for exposing just enough information from {@link XMLStreamWriter} for the purposes of encoding
 * leaf values. While most leaves are simple strings, some (like identityref) need to interact with XML namespaces
 * in order to correctly encode their value.
 *
 * <p>
 * See XMLStreamWriter for description of methods.
 */
abstract class ValueWriter {
    // Additionally ignores null/empty
    abstract void writeCharacters(String text) throws XMLStreamException;

    abstract void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException;

    abstract void writeAttribute(String localName, String value) throws XMLStreamException;

    abstract void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException;

    // Note: lookup results may change if there is other interaction
    abstract NamespaceContext getNamespaceContext();
}
