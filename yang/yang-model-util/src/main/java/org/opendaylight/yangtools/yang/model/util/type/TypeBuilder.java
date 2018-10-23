/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public abstract class TypeBuilder<T extends TypeDefinition<T>> implements Builder<T> {
    private final ImmutableList.Builder<UnknownSchemaNode> unknownSchemaNodes = ImmutableList.builder();
    private final @NonNull SchemaPath path;
    private final @Nullable T baseType;

    TypeBuilder(final @Nullable T baseType, final SchemaPath path) {
        this.path = requireNonNull(path);
        this.baseType = baseType;
    }

    final @Nullable T getBaseType() {
        return baseType;
    }

    final @NonNull SchemaPath getPath() {
        return path;
    }

    final @NonNull List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes.build();
    }

    public final void addUnknownSchemaNode(final @NonNull UnknownSchemaNode node) {
        unknownSchemaNodes.add(node);
    }
}
