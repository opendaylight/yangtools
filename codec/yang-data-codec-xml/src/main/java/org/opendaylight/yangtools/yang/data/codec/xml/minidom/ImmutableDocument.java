/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static java.util.Objects.requireNonNull;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
record ImmutableDocument(Element element) implements Document {
    ImmutableDocument {
        requireNonNull(element);
    }

    static ImmutableDocument of(final XMLStreamReader reader) throws XMLStreamException {
        final int event = reader.getEventType();
        return switch (event) {
            case START_DOCUMENT -> ofDocument(reader);
            case START_ELEMENT -> ofElement(reader);
            default -> throw new XMLStreamException("Unexpected event " + event);
        };
    }

    private static ImmutableDocument ofDocument(final XMLStreamReader reader) throws XMLStreamException {
        int event = reader.nextTag();
        final var document = switch (event) {
            case START_ELEMENT -> ofElement(reader);
            default -> throw new XMLStreamException("Unexpected event " + event);
        };
        event = reader.next();
        return switch (event) {
            case END_DOCUMENT -> document;
            default -> throw new XMLStreamException("Unexpected event " + event);
        };
    }

    private static ImmutableDocument ofElement(final XMLStreamReader reader) throws XMLStreamException {
        throw new UnsupportedOperationException("FIXME: implement this method");
    }
}
