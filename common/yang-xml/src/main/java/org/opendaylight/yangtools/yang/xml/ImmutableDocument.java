/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

import static java.util.Objects.requireNonNull;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
            default -> throw xse(event, reader.getLocation());
        };
    }

    private static ImmutableDocument ofDocument(final XMLStreamReader reader) throws XMLStreamException {
        final int nextTag = reader.nextTag();
        final var document = switch (nextTag) {
            case START_ELEMENT -> ofElement(reader);
            default -> throw xse(nextTag, reader.getLocation());
        };
        final int next = reader.next();
        return switch (next) {
            case END_DOCUMENT -> document;
            default -> throw xse(next, reader.getLocation());
        };
    }

    // Reader is guaranteed to be at START_ELEMENT on entry and END_ELEMENT on exit
    private static ImmutableDocument ofElement(final XMLStreamReader reader) throws XMLStreamException {
        return new ImmutableDocument(elementOf(reader));
    }

    private static Element elementOf(final XMLStreamReader reader) throws XMLStreamException {
        // Fill out localname and namespace
        final var builder = Element.builder()
            .setLocalName(reader.getLocalName())
            .setNamespace(reader.getNamespaceURI());

        // Fill namespace declarations
        for (int i = 0, count = reader.getNamespaceCount(); i < count; ++i) {
            final var prefix = reader.getNamespacePrefix(i);
            final var uri = reader.getNamespaceURI(i);
            builder.addAttribute(prefix == null ? new DefaultNamespaceDeclaration(uri)
                : new PrefixNamespaceDeclaration(prefix, uri));
        }

        // Fill attributes with a shared builder, as it
        final var attrCount = reader.getAttributeCount();
        if (attrCount != 0) {
            // shared builder, as we always initialize all properties
            final var attrBuilder = Attribute.builder();
            for (int i = 0; i < attrCount; ++i) {
                builder.addAttribute(attrBuilder
                    .setLocalName(reader.getAttributeLocalName(i))
                    .setNamespace(reader.getAttributeNamespace(i))
                    .setValue(reader.getAttributeValue(i))
                    .build(reader.getLocation()));
            }
        }

        return readElementBody(reader, builder);
    }

    private static Element readElementBody(final XMLStreamReader reader, final Element.Builder builder)
            throws XMLStreamException {
        while (true) {
            final var next = reader.next();
            final var location = reader.getLocation();
            switch (next) {
                case CDATA:
                case CHARACTERS:
                    builder.addText(reader.getText(), location);
                    break;
                case START_ELEMENT:
                    builder.addElement(elementOf(reader), location);
                    break;
                case END_ELEMENT:
                    return builder.build(location);
                case COMMENT:
                case ENTITY_REFERENCE:
                case PROCESSING_INSTRUCTION:
                case SPACE:
                    // skip
                    break;
                default:
                    throw xse(next, location);
            }
        }
    }

    private static XMLStreamException xse(final int event, final @Nullable Location location) {
        return HackXMLStreamException.of("Unexpected event " + event, location);
    }
}
