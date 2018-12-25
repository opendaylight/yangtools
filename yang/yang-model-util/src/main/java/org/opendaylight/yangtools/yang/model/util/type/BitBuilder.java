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
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

/**
 * Utility builder for {@link Bit} instances.
 *
 * @author Robert Varga
 */
@Beta
public final class BitBuilder implements Builder<Bit>, Mutable {
    private final SchemaPath schemaPath;
    private final Long position;

    private ImmutableList<UnknownSchemaNode> unknownSchemaNodes = ImmutableList.of();
    private Status status = Status.CURRENT;
    private String description;
    private String reference;

    private BitBuilder(final SchemaPath schemaPath, final Long position) {
        this.schemaPath = requireNonNull(schemaPath);
        this.position = requireNonNull(position);
    }

    public static BitBuilder create(final SchemaPath schemaPath, final Long position) {
        return new BitBuilder(schemaPath, position);
    }

    public BitBuilder setDescription(final String description) {
        this.description = description;
        return this;
    }

    public BitBuilder setReference(final String reference) {
        this.reference = reference;
        return this;
    }

    public BitBuilder setStatus(final Status status) {
        this.status = requireNonNull(status);
        return this;
    }

    public BitBuilder setUnknownSchemaNodes(final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    public BitBuilder setUnknownSchemaNodes(final UnknownSchemaNode... unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    @Override
    public Bit build() {
        return new BitImpl(schemaPath, position, description, reference, status, unknownSchemaNodes);
    }
}
