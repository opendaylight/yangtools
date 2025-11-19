/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;

/**
 * Immutable on-heap implementation of {@link NormalizedYangData}.
 */
final class ImmutableYangData implements Immutable, NormalizedYangData {
    private final @NonNull YangDataName name;
    private final @NonNull DataContainerChild child;

    ImmutableYangData(final YangDataName name, final DataContainerChild child) {
        this.name = requireNonNull(name);
        this.child = requireNonNull(child);
    }

    @Override
    public YangDataName name() {
        return name;
    }

    @Override
    public Collection<DataContainerChild> body() {
        return List.of(child);
    }

    @Override
    public DataContainerChild childByArg(final NodeIdentifier key) {
        return key.equals(child.name()) ? child : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, child);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof NormalizedYangData other && name.equals(other.name())
            && equalBody(other.body());
    }

    private boolean equalBody(final Collection<? extends DataContainerChild> otherBody) {
        final var it = otherBody.iterator();
        return it.hasNext() && child.equals(it.next()) && !it.hasNext();
    }

    @Override
    public String toString() {
        // mirrors MoreObjects.toStringHelper() to keep consistency with other classes in this package, but does not
        // instantiate body()
        return "ImmutableYangData{name=" + name + ", body=[" + child + "]}";
    }
}
