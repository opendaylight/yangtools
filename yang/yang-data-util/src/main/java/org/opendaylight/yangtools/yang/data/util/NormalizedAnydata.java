/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * The contents of an {@code anydata} node in a normalized format. This representation acts as a schema-bound bridge
 * between the various (mostly parser-based) representations.
 *
 * <p>
 * Note this class (and all of its subclasses) rely on identity for their equality contract.
 */
@Beta
@NonNullByDefault
public class NormalizedAnydata implements Immutable {
    private final DataSchemaContextNode<?> contextNode;
    private final DataSchemaContextTree contextTree;
    private final NormalizedNode<?, ?> data;

    public NormalizedAnydata(final DataSchemaContextTree contextTree, final DataSchemaContextNode<?> contextNode,
            final NormalizedNode<?, ?> data) {
        this.contextTree = requireNonNull(contextTree);
        this.contextNode = requireNonNull(contextNode);
        this.data = requireNonNull(data);
    }

    public final DataSchemaContextNode<?> getContextNode() {
        return contextNode;
    }

    public final DataSchemaContextTree getContextTree() {
        return contextTree;
    }

    public final NormalizedNode<?, ?> getData() {
        return data;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("tree", contextTree).add("node", contextNode).add("data", data);
    }
}
