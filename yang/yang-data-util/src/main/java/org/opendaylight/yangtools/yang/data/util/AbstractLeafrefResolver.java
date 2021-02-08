/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.data.util.codec.AbstractCodecFactory.LeafrefResolver;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

@Beta
abstract class AbstractLeafrefResolver implements LeafrefResolver, Mutable {
    final SchemaInferenceStack dataTree;

    AbstractLeafrefResolver(final SchemaInferenceStack dataTree) {
        this.dataTree = requireNonNull(dataTree);
    }

    @Override
    public final TypeDefinition<?> resolveLeafref(final LeafrefTypeDefinition type) {
        final EffectiveStatement<?, ?> stmt = toSchemaInferenceStack().resolvePathExpression(type.getPathStatement());
        checkArgument(stmt instanceof TypeAware, "Unexpected result %s", stmt);
        return ((TypeAware) stmt).getType();
    }

    /**
     * Return a copy of current state as an {@link SchemaInferenceStack}.
     *
     * @return A SchemaInferenceStack
     */
    public final @NonNull SchemaInferenceStack toSchemaInferenceStack() {
        return dataTree.copy();
    }
}
