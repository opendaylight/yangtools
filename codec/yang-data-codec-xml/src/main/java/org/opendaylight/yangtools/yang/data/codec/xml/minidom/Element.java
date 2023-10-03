/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.xml.stream.XMLStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A single XML element. This interface should not be implemented directly, but rather indirectly through its
 * {@link ContainerElement} and {@link TextElement} specializations.
 */
public sealed interface Element extends Node permits ContainerElement, TextElement, ImmutableElement, W3CElement {
    /**
     * This element's attributes.
     *
     * @return List of attributes
     */
    @NonNull List<@NonNull Attribute> attributes();

    default @Nullable Attribute attribute(final @Nullable String namespace, final @NonNull String localName) {
        requireNonNull(localName);
        for (var attr : attributes()) {
            if (localName.equals(attr.localName()) && Objects.equals(namespace, attr.namespace())) {
                return attr;
            }
        }
        return null;
    }

    default @NonNull Attribute getAttribute(final @Nullable String namespace, final @NonNull  String localName) {
        final var attr = attribute(namespace, localName);
        if (attr == null) {
            throw new NoSuchElementException("Cannot find (%s)%s".formatted(namespace, localName));
        }
        return attr;
    }

    default @Nullable String attributeValue(final @Nullable String namespace, final @NonNull  String localName) {
        // We need to distinguish attribute presence vs. empty attributes
        final var attr = attribute(namespace, localName);
        return attr != null ? attr.value() : null;
    }

    static @NonNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private final ImmutableList.Builder<Attribute> attributes = ImmutableList.builder();
        private final @NonNull List<@NonNull Element> elements = new ArrayList<>();
        private final @NonNull List<@NonNull String> texts = new ArrayList<>();
        private String namespace;
        private String localName;

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

        public @NonNull Builder addAttribute(final Attribute attribute) {
            attributes.add(attribute);
            return this;
        }

        public @NonNull Builder addElement(final Element element) throws XMLStreamException {
            if (!texts.isEmpty()) {
                throw new XMLStreamException(
                    "Element already has %s text items, cannot add element".formatted(texts.size()));
            }
            elements.add(requireNonNull(element));
            return this;
        }

        public @NonNull Builder addText(final String text) throws XMLStreamException {
            if (!elements.isEmpty()) {
                throw new XMLStreamException(
                    "Element already has %s child elements, cannot add text".formatted(elements.size()));
            }
            // ignore empty text elements
            if (!text.isEmpty()) {
                texts.add(text);
            }
            return this;
        }

        public @NonNull Element build() throws XMLStreamException {
            final var ln = localName;
            if (ln == null) {
                throw new XMLStreamException("localName not set");
            }

            return switch (texts.size()) {
                case 0 -> new ImmutableContainerElement(namespace, ln, attributes.build(), elements);
                case 1 -> new ImmutableTextElement(namespace, ln, attributes.build(), texts.get(0));
                default -> {
                    final var sb = new StringBuilder();
                    texts.forEach(sb::append);
                    yield new ImmutableTextElement(namespace, ln, attributes.build(), sb.toString());
                }
            };
        }
    }
}
