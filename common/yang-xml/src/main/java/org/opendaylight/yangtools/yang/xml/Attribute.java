/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

import static java.util.Objects.requireNonNull;

import javax.xml.XMLConstants;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A single XML attribute.
 */
public non-sealed interface Attribute extends Node {
    /**
     * This attribute's value.
     *
     * @return Attribute value
     */
    @NonNull String value();

    /**
     * Convenience method for identifying namespace declarations. Implementations are required to examine this
     * attribute's name for two things:
     * <ol>
     *   <li>whether this is a declaration of the default namespace, as defined by {@link #isDefaultNamespace()}<li>
     *   <li>whether this is a declaration of a namespace prefix, as defined by {@link #isNamespacePrefix()}</li>
     * </ol>
     *
     * @return {@code true} if this attribute represents a namespace declaration.
     */
    default boolean isNamespace() {
        return isDefaultNamespace() || isNamespacePrefix();
    }

    /**
     * Convenience method for identifying default namespace declarations. Implementations are required to examine this
     * attribute's name for two things:
     * <ol>
     *   <li>the {@link #namespace()} is {@code null}</li>
     *   <li>the {@link #localName()} is {@value XMLConstants#XMLNS_ATTRIBUTE}</li>
     * </ol>
     *
     * @return {@code true} if this attribute represents a default namespace declaration.
     */
    default boolean isDefaultNamespace() {
        return namespace() == null && XMLConstants.XMLNS_ATTRIBUTE.equals(localName());
    }

    /**
     * Convenience method for identifying namespace prefix declarations. Implementations are required to match this
     * attribute's {@link #namespace()} to {@link XMLConstants#XMLNS_ATTRIBUTE_NS_URI}.
     *
     * @return {@code true} if this attribute represents a namespace prefix declaration.
     */
    default boolean isNamespacePrefix() {
        return XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace());
    }

    static @NonNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private String namespace;
        private String localName;
        private String value;

        Builder() {
            // Hidden on purpose
        }

        public @NonNull Builder setLocalName(final String localName) {
            this.localName = requireNonNull(localName);
            return this;
        }

        public @NonNull Builder setNamespace(final String namespace) {
            this.namespace = namespace;
            return this;
        }

        public @NonNull Builder setValue(final String value) {
            this.value = requireNonNull(value);
            return this;
        }

        public @NonNull Attribute build(final @Nullable Location location) throws XMLStreamException {
            final var ln = localName;
            if (ln == null) {
                throw HackXMLStreamException.of("localName not set", location);
            }
            final var lv = value;
            if (lv == null) {
                throw HackXMLStreamException.of("value not set", location);
            }
            return new ImmutableAttribute(namespace, ln, lv);
        }
    }
}
