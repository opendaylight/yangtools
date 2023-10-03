/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An immutable implementation of {@link Node}.
 */
@NonNullByDefault
abstract sealed class ImmutableNode implements Node permits ImmutableAttribute, ImmutableElement {
    private final @Nullable String namespace;
    private final String localName;

    ImmutableNode(final @Nullable String namespace, final String localName) {
        this.namespace = namespace;
        this.localName = requireNonNull(localName);
    }

    @Override
    public final @Nullable String namespace() {
        return namespace;
    }

    @Override
    public final String localName() {
        return localName;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("name", namespace != null ? "(" + namespace + ")" + localName : localName);
    }
}
