/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A single XML element. This interface should not be implemented directly, but rather indirectly through its
 * {@link ContainerElement} and {@link TextElement} specializations.
 */
@NonNullByDefault
public sealed interface Element extends Node permits ContainerElement, TextElement, ImmutableElement, W3CElement {
    /**
     * This element's attributes.
     *
     * @return List of attributes
     */
    List<Attribute> attributes();

    default @Nullable Attribute attribute(final @Nullable String namespace, final String localName) {
        requireNonNull(localName);
        for (var attr : attributes()) {
            if (localName.equals(attr.localName()) && Objects.equals(namespace, attr.namespace())) {
                return attr;
            }
        }
        return null;
    }

    default Attribute getAttribute(final @Nullable String namespace, final String localName) {
        final var attr = attribute(namespace, localName);
        if (attr == null) {
            throw new NoSuchElementException("Cannot find (%s)%s".formatted(namespace, localName));
        }
        return attr;
    }

    default @Nullable String attributeValue(final @Nullable String namespace, final String localName) {
        // We need to distinguish attribute presence vs. empty attributes
        final var attr = attribute(namespace, localName);
        return attr != null ? attr.value() : null;
    }
}
