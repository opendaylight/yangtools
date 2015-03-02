/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class DataSchemaContextTree {

    private static final LoadingCache<SchemaContext, DataSchemaContextTree> TREES = CacheBuilder.newBuilder()
            .weakKeys()
            .build(new CacheLoader<SchemaContext, DataSchemaContextTree>() {

                @Override
                public DataSchemaContextTree load(SchemaContext key) throws Exception {
                    return new DataSchemaContextTree(key);
                }

            });

    private final DataSchemaContextNode<?> root;

    private DataSchemaContextTree(final SchemaContext ctx) {
        root = DataSchemaContextNode.from(ctx);
    }


    public static DataSchemaContextTree from(SchemaContext ctx) {
        return TREES.getUnchecked(ctx);
    }

    public YangInstanceIdentifier toNormalized(final YangInstanceIdentifier legacy) {
        ImmutableList.Builder<PathArgument> normalizedArgs = ImmutableList.builder();

        DataSchemaContextNode<?> currentOp = root;
        Iterator<PathArgument> arguments = legacy.getPathArguments().iterator();

        while (arguments.hasNext()) {
            PathArgument legacyArg = arguments.next();
            currentOp = currentOp.getChild(legacyArg);
            checkArgument(currentOp != null,
                    "Legacy Instance Identifier %s is not correct. Normalized Instance Identifier so far %s", legacy,
                    normalizedArgs.build());
            while (currentOp.isMixin()) {
                normalizedArgs.add(currentOp.getIdentifier());
                currentOp = currentOp.getChild(legacyArg.getNodeType());
            }
            normalizedArgs.add(legacyArg);
        }

        return YangInstanceIdentifier.create(normalizedArgs.build());
    }

    public DataSchemaContextNode<?> getOperation(final YangInstanceIdentifier legacy) {
        DataSchemaContextNode<?> currentOp = root;
        Iterator<PathArgument> arguments = legacy.getPathArguments().iterator();

        while (arguments.hasNext()) {
            currentOp = currentOp.getChild(arguments.next());
        }
        return currentOp;
    }

    public YangInstanceIdentifier toLegacy(final YangInstanceIdentifier normalized) {
        ImmutableList.Builder<PathArgument> legacyArgs = ImmutableList.builder();
        DataSchemaContextNode<?> currentOp = root;
        for (PathArgument normalizedArg : normalized.getPathArguments()) {
            currentOp = currentOp.getChild(normalizedArg);
            if (!currentOp.isMixin()) {
                legacyArgs.add(normalizedArg);
            }
        }
        return YangInstanceIdentifier.create(legacyArgs.build());
    }

    public DataSchemaContextNode<?> getRootOperation() {
        return root;
    }

}
