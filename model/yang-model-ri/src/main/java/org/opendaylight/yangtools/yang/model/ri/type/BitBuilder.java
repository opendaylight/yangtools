/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

/**
 * Utility builder for {@link Bit} instances.
 *
 * @author Robert Varga
 */
@Beta
public final class BitBuilder implements Mutable {
    private final String name;
    private final Uint32 position;

    private ImmutableList<UnknownSchemaNode> unknownSchemaNodes = ImmutableList.of();
    private Status status = Status.CURRENT;
    private String description;
    private String reference;

    private BitBuilder(final String name, final Uint32 position) {
        this.name = requireNonNull(name);
        this.position = requireNonNull(position);
    }

    public static @NonNull BitBuilder create(final String name, final Uint32 position) {
        return new BitBuilder(name, position);
    }

    public @NonNull BitBuilder setDescription(final String description) {
        this.description = description;
        return this;
    }

    public @NonNull BitBuilder setReference(final String reference) {
        this.reference = reference;
        return this;
    }

    public @NonNull BitBuilder setStatus(final Status status) {
        this.status = requireNonNull(status);
        return this;
    }

    public @NonNull BitBuilder setUnknownSchemaNodes(final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    public @NonNull BitBuilder setUnknownSchemaNodes(final UnknownSchemaNode... unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    /**
     * Return the {@link Bit} corresponding to current builder state.
     *
     * @return A Bit
     */
    public @NonNull Bit build() {
        return new BitImpl(name, position, description, reference, status, unknownSchemaNodes);
    }
}
