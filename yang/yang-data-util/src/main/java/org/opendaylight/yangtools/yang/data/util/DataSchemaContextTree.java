/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Semantic tree binding a {@link SchemaContext} to a {@link NormalizedNode} tree. Since the layout of the schema
 * and data has differences, the mapping is not trivial -- which is where this class comes in.
 *
 * @author Robert Varga
 */
// FIXME: 3.0.0: @NonNullByDefault
public final class DataSchemaContextTree {
    private static final LoadingCache<SchemaContext, DataSchemaContextTree> TREES = CacheBuilder.newBuilder()
            .weakKeys().weakValues().build(new CacheLoader<SchemaContext, DataSchemaContextTree>() {
                @Override
                public DataSchemaContextTree load(final SchemaContext key) throws Exception {
                    return new DataSchemaContextTree(key);
                }
            });

    private final DataSchemaContextNode<?> root;

    private DataSchemaContextTree(final SchemaContext ctx) {
        root = DataSchemaContextNode.from(ctx);
    }

    public static @NonNull DataSchemaContextTree from(final @NonNull SchemaContext ctx) {
        return TREES.getUnchecked(ctx);
    }

    /**
     * Find a child node as identified by an absolute {@link YangInstanceIdentifier}.
     *
     * @param path Path towards the child node
     * @return Child node if present, or empty when corresponding child is not found.
     * @throws NullPointerException if {@code path} is null
     */
    public @NonNull Optional<@NonNull DataSchemaContextNode<?>> findChild(final @NonNull YangInstanceIdentifier path) {
        return getRoot().findChild(path);
    }

    /**
     * Get a child node as identified by an absolute {@link YangInstanceIdentifier}.
     *
     * @param path Path towards the child node
     * @return Child node if present, or null when corresponding child is not found.
     * @throws NullPointerException if {@code path} is null
     *
     * @deprecated Use {@link #findChild(YangInstanceIdentifier)} instead.
     */
    @Deprecated
    public @Nullable DataSchemaContextNode<?> getChild(final YangInstanceIdentifier path) {
        DataSchemaContextNode<?> currentOp = root;
        for (PathArgument arg : path.getPathArguments()) {
            currentOp = currentOp.getChild(arg);
            if (currentOp == null) {
                return null;
            }
        }
        return currentOp;
    }

    public DataSchemaContextNode<?> getRoot() {
        return root;
    }
}
