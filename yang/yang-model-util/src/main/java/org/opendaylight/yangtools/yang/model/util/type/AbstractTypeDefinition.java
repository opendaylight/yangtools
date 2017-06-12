/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

abstract class AbstractTypeDefinition<T extends TypeDefinition<T>> implements Immutable, TypeDefinition<T> {
    private final List<UnknownSchemaNode> unknownSchemaNodes;
    private final SchemaPath path;

    AbstractTypeDefinition(final SchemaPath path, final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        this.path = Preconditions.checkNotNull(path);
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
    }

    @Nonnull
    @Override
    public final QName getQName() {
        return path.getLastComponent();
    }

    @Nonnull
    @Override
    public final SchemaPath getPath() {
        return path;
    }

    @Nonnull
    @Override
    public final List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
