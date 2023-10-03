/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.xml.UntrustedXML;

/**
 * A complete XML document. It has exactly one {@link #element() document element}.
 */
// FIXME: This is not quite sufficient, as we need to account for a NamespaceContext being present at the root of
//        the document -- which is to say that there may be pre-existing declarations present via an external means.
//        this is especially true when we get constructed from a XMLStreamReader ELEMENT event: at that point we have
//        some namespace declarations from parent and those should be tracked separately -- they are effective in that
//        element, but are NOT part of its declaration.
//        This will become more obvious as we provide the writeout bits, where target host is important. Imagine a case
//        where we pick up an 'anydata' from a document which declares all its namespaces at its document element:
//        the anydata element will not have any of them anywhere. Now if we want to place that 'anydata' element into
//        its own XML document, we need to make sure all those namespace declarations are retained and we do not make
//        anything up. Most notably the captured context needs to override any declarations made in the target document
//        to ensure the content of text elements can be reference those declarations.
public interface Document {
    /**
     * Return this document's root {@link Element}.
     *
     * @return An {@link Element}.
     */
    @NonNull Element element();

    /**
     * Create a {@link Document} by interpreting a {@link String} as an XML document.
     *
     * @param document A string with the contents of the intended document
     * @return A {@link Document}
     * @throws XMLStreamException if an error occurs
     */
    static @NonNull Document of(final String document) throws XMLStreamException {
        return of(new StringReader(document));
    }

    /**
     * Create a {@link Document} by interpreting a {@link Reader} as an XML document.
     *
     * @param reader backing {@link Reader}
     * @return A {@link Document}
     * @throws XMLStreamException if an error occurs
     */
    static @NonNull Document of(final Reader reader) throws XMLStreamException {
        return of(UntrustedXML.createXMLStreamReader(reader));
    }

    /**
     * Create a {@link Document} by interpreting an {@link InputStream} as an XML document.
     *
     * @param stream backing {@link InputStream}
     * @return A {@link Document}
     * @throws XMLStreamException if an error occurs
     */
    static @NonNull Document of(final InputStream stream) throws XMLStreamException {
        return of(UntrustedXML.createXMLStreamReader(stream));
    }

    /**
     * Create a {@link Document} by interpreting an {@link InputStream} as an XML document, using the supplied
     * {@link Charset}.
     *
     * @param stream backing {@link InputStream}
     * @param charset {@link Charset} to use
     * @return A {@link Document}
     * @throws XMLStreamException if an error occurs
     */
    static @NonNull Document of(final InputStream stream, final Charset charset) throws XMLStreamException {
        return of(UntrustedXML.createXMLStreamReader(stream, charset));
    }

    /**
     * Create a {@link Document} by interpreting an {@link InputStream} as an XML document, using the supplied
     * {@link Charset}.
     *
     * @param reader backing {@link XMLStreamReader}
     * @return A {@link Document}
     * @throws XMLStreamException if an error occurs
     */
    static @NonNull Document of(final XMLStreamReader reader)  throws XMLStreamException {
        return ImmutableDocument.of(requireNonNull(reader));
    }

    /**
     * Return a new {@link Builder}.
     *
     * @return a new {@link Builder}
     */
    static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Builder of an immutable implementation of {@link Document}.
     */
    final class Builder {
        private Element element;

        Builder() {
            // Hidden on purpose
        }

        public Builder setElement(final Element element) {
            this.element = requireNonNull(element);
            return this;
        }

        public @NonNull Document build() throws XMLStreamException {
            return build(null);
        }

        public @NonNull Document build(final @Nullable Location location) throws XMLStreamException {
            final var local = element;
            if (local == null) {
                throw HackXMLStreamException.of("A document element is required", location);
            }
            return new ImmutableDocument(local);
        }
    }
}
