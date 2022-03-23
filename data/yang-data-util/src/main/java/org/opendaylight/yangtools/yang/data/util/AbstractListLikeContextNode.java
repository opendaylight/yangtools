/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * An {@link AbstractMixinContextNode} which corresponding to a {@code list} or {@code leaf-list} node. NormalizedNode
 * representation of these nodes is similar to JSON encoding and therefore we have two {@link DataSchemaContextNode}
 * levels backed by a single {@link DataSchemaNode}.
 */
abstract class AbstractListLikeContextNode<T extends PathArgument> extends AbstractMixinContextNode<T> {
    AbstractListLikeContextNode(final T identifier, final DataSchemaNode schema) {
        super(identifier, schema);
    }

    @Override
    protected final DataSchemaContextNode<?> enterChild(final QName child, final SchemaInferenceStack stack) {
        // Stack is already pointing to the corresponding statement, now we are just working with the child
        return getChild(child);
    }

    @Override
    protected final DataSchemaContextNode<?> enterChild(final PathArgument child, final SchemaInferenceStack stack) {
        return getChild(child);
    }
}
