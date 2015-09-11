/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * An extension of {@link NormalizedNodeStreamSink}, which can performs schema-informed processing. Users of this
 * interface can inform the sink of the {@link SchemaNode} of the element which is about be started via
 * {@link #nextNodeSchema(SchemaNode)}. Implementations which require a schema node to be present may throw
 * {@link IllegalStateException} whenever an attempt at opening a node is made without
 * {@link #nextNodeSchema(SchemaNode)} having been invoked.
 *
 * The schema node is logically cleared when {@link #endNode()} is called.
 */
public interface SchemaAwareNormalizedNodeStreamSink extends NormalizedNodeStreamSink {
    /**
     * Inform the implementation of the {@link SchemaNode} describing the next node which will be started.
     *
     * @param schema Schema of the next node
     * @throws NullPointerException when the argument is null
     * @throws IllegalStateException when this method is invoked for a second consecutive time without a node being
     *                               open.
     */
    void nextNodeSchema(@Nonnull SchemaNode schema);
}
