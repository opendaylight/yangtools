/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

final class EnumPairImpl implements EnumPair, Immutable {
    private final List<UnknownSchemaNode> unknownSchemaNodes;
    private final String description;
    private final String reference;
    private final Status status;
    private final String name;
    private final int value;

    EnumPairImpl(final String name, final int value, final String description, final String reference,
            final Status status, final List<UnknownSchemaNode> unknownSchemaNodes) {
        this.name = Preconditions.checkNotNull(name);
        this.value = value;
        this.description = description;
        this.reference = reference;
        this.status = Preconditions.checkNotNull(status);
        this.unknownSchemaNodes = Preconditions.checkNotNull(unknownSchemaNodes);
    }

    @Nonnull
    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Nonnull
    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + unknownSchemaNodes.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + Integer.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EnumPair)) {
            return false;
        }
        EnumPair other = (EnumPair) obj;
        if (!Objects.equals(name, other.getName())) {
            return false;
        }

        return value == other.getValue() && Objects.equals(unknownSchemaNodes, other.getUnknownSchemaNodes());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("value", value).toString();
    }
}
