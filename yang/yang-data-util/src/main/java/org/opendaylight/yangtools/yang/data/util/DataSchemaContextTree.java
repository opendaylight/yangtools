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
import java.util.Iterator;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public final class DataSchemaContextTree {
    private static final LoadingCache<SchemaContext, DataSchemaContextTree> TREES = CacheBuilder.newBuilder()
            .weakKeys()
            .build(new CacheLoader<SchemaContext, DataSchemaContextTree>() {

                @Override
                public DataSchemaContextTree load(final SchemaContext key) throws Exception {
                    return new DataSchemaContextTree(key);
                }

            });

    private final DataSchemaContextNode<?> root;

    private DataSchemaContextTree(final SchemaContext ctx) {
        root = DataSchemaContextNode.from(ctx);
    }

    @Nonnull public static DataSchemaContextTree from(@Nonnull final SchemaContext ctx) {
        return TREES.getUnchecked(ctx);
    }

    public DataSchemaContextNode<?> getChild(final YangInstanceIdentifier path) {
        DataSchemaContextNode<?> currentOp = root;
        Iterator<PathArgument> arguments = path.getPathArguments().iterator();
        while (arguments.hasNext()) {
            currentOp = currentOp.getChild(arguments.next());
        }
        return currentOp;
    }

    public DataSchemaContextNode<?> getRoot() {
        return root;
    }
}
