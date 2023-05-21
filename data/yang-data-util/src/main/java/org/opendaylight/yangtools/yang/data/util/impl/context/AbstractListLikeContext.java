/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.context;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * An {@link AbstractPathMixinContext} which corresponding to a {@code list} or {@code leaf-list} node. NormalizedNode
 * representation of these nodes is similar to JSON encoding and therefore we have two {@link DataSchemaContext} levels
 * backed by a single {@link DataSchemaNode}.
 */
abstract sealed class AbstractListLikeContext extends AbstractPathMixinContext
        permits LeafListContext, ListContext, MapContext {
    private final AbstractContext child;

    AbstractListLikeContext(final DataSchemaNode schema, final AbstractContext child) {
        super(schema);
        this.child = requireNonNull(child);
    }

    @Override
    public final AbstractContext childByQName(final QName qname) {
        return qname.equals(dataSchemaNode.getQName()) ? child : null;
    }

    @Override
    public abstract AbstractContext childByArg(PathArgument arg);

    // Stack is already pointing to the corresponding statement, now we are just working with the child
    @Override
    public final AbstractContext enterChild(final SchemaInferenceStack stack, final QName qname) {
        requireNonNull(stack);
        return childByQName(qname);
    }

    @Override
    public final AbstractContext enterChild(final SchemaInferenceStack stack, final PathArgument arg) {
        requireNonNull(stack);
        return childByArg(arg);
    }
}
