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
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * Utility builder for {@link EnumPair} instances.
 */
@Beta
public final class EnumPairBuilder implements Mutable {
    private final String name;
    private final Integer value;

    private ImmutableList<UnknownSchemaNode> unknownSchemaNodes = ImmutableList.of();
    private Status status = Status.CURRENT;
    private String description;
    private String reference;

    private EnumPairBuilder(final String name, final Integer value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }

    public static @NonNull EnumPairBuilder create(final String name, final Integer value) {
        return new EnumPairBuilder(name, value);
    }

    public @NonNull EnumPairBuilder setDescription(final String description) {
        this.description = description;
        return this;
    }

    public @NonNull EnumPairBuilder setReference(final String reference) {
        this.reference = reference;
        return this;
    }

    public @NonNull EnumPairBuilder setStatus(final Status status) {
        this.status = requireNonNull(status);
        return this;
    }

    public @NonNull EnumPairBuilder setUnknownSchemaNodes(final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    public @NonNull EnumPairBuilder setUnknownSchemaNodes(final UnknownSchemaNode... unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    /**
     * Return an {@link EnumPair} representation of this builder's current state.
     *
     * @return An EnumPair
     */
    public @NonNull EnumPair build() {
        return new EnumPairImpl(name, value, description, reference, status, unknownSchemaNodes);
    }
}
