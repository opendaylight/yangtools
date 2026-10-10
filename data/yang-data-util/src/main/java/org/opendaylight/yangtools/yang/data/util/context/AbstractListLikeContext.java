/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree.Step;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * An {@link AbstractPathMixinContext} which corresponding to a {@code list} or {@code leaf-list} node. NormalizedNode
 * representation of these nodes is similar to JSON encoding and therefore we have two {@link DataSchemaContext} levels
 * backed by a single {@link DataSchemaNode}.
 */
abstract sealed class AbstractListLikeContext extends AbstractPathMixinContext
        permits LeafListContext, ListContext, MapContext {
    private final @NonNull AbstractContext childContext;

    AbstractListLikeContext(final DataSchemaNode schema, final AbstractContext childContext) {
        super(schema);
        this.childContext = requireNonNull(childContext);
    }

    @Override
    public abstract AbstractContext childByArg(PathArgument arg);

    @Override
    public final AbstractContext childByQName(final QName qname) {
        return qname.equals(dataSchemaNode.getQName()) ? childContext : null;
    }

    // Stack is already pointing to the corresponding statement, now we are just working with the child
    @Override
    public final AbstractContext enterChild(final SchemaInferenceStack stack, final QName child) {
        requireNonNull(stack);
        return childByQName(child);
    }

    @Override
    public final AbstractContext enterChild(final SchemaInferenceStack stack, final PathArgument child) {
        requireNonNull(stack);
        return childByArg(child);
    }

    @Override
    public final Step.OfEntry stepTo(final QName child) {
        return child.equals(dataSchemaNode.getQName()) ? new Step.OfEntry(childContext) : null;
    }
}
