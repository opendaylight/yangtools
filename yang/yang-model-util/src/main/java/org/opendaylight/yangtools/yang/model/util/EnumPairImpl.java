/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

public final class EnumPairImpl implements EnumPair, Immutable {
    private final List<UnknownSchemaNode> unknownSchemaNodes;
    private final String description;
    private final String reference;
    private final SchemaPath path;
    private final Status status;
    private final Integer value;
    private final String name;

    public EnumPairImpl(final String name, final Integer value, final SchemaPath path, final String description,
            final String reference, final Status status, final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        this.path = Preconditions.checkNotNull(path);
        this.value = Preconditions.checkNotNull(value);
        this.name = Preconditions.checkNotNull(name);
        this.description = description;
        this.reference = reference;
        this.status = Preconditions.checkNotNull(status);
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
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

    @Override
    public Status getStatus() {
        return status;
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
        result = prime * result + Objects.hashCode(path.getLastComponent());
        result = prime * result + Objects.hashCode(path);
        result = prime * result + Objects.hashCode(unknownSchemaNodes);
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(value);
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

        return Objects.equals(value, other.getValue()) && Objects.equals(getPath(), other.getPath()) &&
                Objects.equals(unknownSchemaNodes, other.getUnknownSchemaNodes());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("value", value).toString();
    }
}
