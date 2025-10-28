/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

final class BitImpl implements Bit, Immutable {
    private final @NonNull ImmutableList<UnknownSchemaNode> unknownNodes;
    private final @NonNull String name;
    private final String description;
    private final String reference;
    private final @NonNull Status status;
    private final @NonNull Uint32 position;

    BitImpl(final String name, final Uint32 position, final String description,
            final String reference, final Status status, final List<UnknownSchemaNode> unknownNodes) {
        this.name = requireNonNull(name);
        this.position = requireNonNull(position);
        this.description = description;
        this.reference = reference;
        this.status = requireNonNull(status);
        this.unknownNodes = ImmutableList.copyOf(unknownNodes);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Collection<? extends UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public Uint32 getPosition() {
        return position;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + position.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Bit other
            && name.equals(other.getName()) && position == other.getPosition();
    }

    @Override
    public String toString() {
        return Bit.class.getSimpleName() + "[name=" + name + ", position=" + position + "]";
    }
}
