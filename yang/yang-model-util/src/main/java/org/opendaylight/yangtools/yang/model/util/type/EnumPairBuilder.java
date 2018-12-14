/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * Utility builder for {@link EnumPair} instances.
 *
 * @author Robert Varga
 */
@Beta
public final class EnumPairBuilder implements Builder<EnumPair>, Mutable {
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

    public static EnumPairBuilder create(final String name, final Integer value) {
        return new EnumPairBuilder(name, value);
    }

    public EnumPairBuilder setDescription(final String description) {
        this.description = description;
        return this;
    }

    public EnumPairBuilder setReference(final String reference) {
        this.reference = reference;
        return this;
    }

    public EnumPairBuilder setStatus(final Status status) {
        this.status = Preconditions.checkNotNull(status);
        return this;
    }

    public EnumPairBuilder setUnknownSchemaNodes(final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    public EnumPairBuilder setUnknownSchemaNodes(final UnknownSchemaNode... unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    @Override
    public EnumPair build() {
        return new EnumPairImpl(name, value, description, reference, status, unknownSchemaNodes);
    }
}
