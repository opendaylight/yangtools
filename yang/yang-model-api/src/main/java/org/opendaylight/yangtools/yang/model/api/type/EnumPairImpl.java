/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * Internal implementation of EnumPair.
 */
final class EnumPairImpl implements EnumPair, Immutable {
    private final List<UnknownSchemaNode> unknownNodes;
    private final String description;
    private final String reference;
    private final SchemaPath path;
    private final Status status;
    private final Integer value;
    private final String name;

    EnumPairImpl(final SchemaPath path, final String description, final String reference, final Status status,
            final String name, final Integer value, final List<UnknownSchemaNode> unknownNodes) {
        this.path = Preconditions.checkNotNull(path);
        this.description = description;
        this.reference = reference;
        this.status = Preconditions.checkNotNull(status);
        this.name = Preconditions.checkNotNull(name);
        this.value = value;
        this.unknownNodes = ImmutableList.copyOf(unknownNodes);
    }

    @Override
    public QName getQName() {
        return path.getLastComponent();
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(path);
        result = prime * result + Objects.hashCode(unknownNodes);
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EnumPairImpl)) {
            return false;
        }
        EnumPairImpl other = (EnumPairImpl) obj;
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        if (!Objects.equals(unknownNodes, other.unknownNodes)) {
            return false;
        }
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return EnumPair.class.getSimpleName() + "[name=" + name + ", value=" + value + "]";
    }
}
