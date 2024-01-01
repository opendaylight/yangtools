/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.CheckedValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.data.util.impl.context.ContainerContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Semantic tree binding a {@link EffectiveModelContext} to a {@link NormalizedNode} tree. Since the layout of the
 * schema and data has differences, the mapping is not trivial -- which is where this class comes in.
 */
public final class DataSchemaContextTree {
    public record NodeAndStack(@NonNull DataSchemaContext node, @NonNull SchemaInferenceStack stack) {
        public NodeAndStack(final @NonNull DataSchemaContext node, final @NonNull SchemaInferenceStack stack) {
            this.node = requireNonNull(node);
            this.stack = requireNonNull(stack);
        }
    }

    private static final LoadingCache<EffectiveModelContext, @NonNull DataSchemaContextTree> TREES =
        CacheBuilder.newBuilder().weakKeys().weakValues().build(new CacheLoader<>() {
            @Override
            public DataSchemaContextTree load(final EffectiveModelContext key) {
                return new DataSchemaContextTree(key);
            }
        });

    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull ContainerContext root;

    private DataSchemaContextTree(final EffectiveModelContext modelContext) {
        this.modelContext = requireNonNull(modelContext);
        root = new ContainerContext(modelContext);
    }

    public static @NonNull DataSchemaContextTree from(final @NonNull EffectiveModelContext ctx) {
        return TREES.getUnchecked(ctx);
    }

    public @NonNull EffectiveModelContext modelContext() {
        return modelContext;
    }

    /**
     * Find a child node as identified by an absolute {@link YangInstanceIdentifier}.
     *
     * @param path Path towards the child node
     * @return Child node if present, or {@code null} when corresponding child is not found.
     * @throws NullPointerException if {@code path} is {@code null}
     */
    public @Nullable DataSchemaContext childByPath(final @NonNull YangInstanceIdentifier path) {
        return root.childByPath(path);
    }

    /**
     * Find a child node as identified by an absolute {@link YangInstanceIdentifier}.
     *
     * @param path Path towards the child node
     * @return Child node if present, or empty when corresponding child is not found.
     * @throws NullPointerException if {@code path} is {@code null}
     */
    public @NonNull Optional<@NonNull DataSchemaContext> findChild(final @NonNull YangInstanceIdentifier path) {
        // Optional.ofNullable() inline due to annotations
        final var child = root.childByPath(path);
        return child == null ? Optional.empty() : Optional.of(child);
    }

    /**
     * Find a child node as identified by an absolute {@link YangInstanceIdentifier} and return it along with a suitably
     * initialized {@link SchemaInferenceStack}.
     *
     * @param path Path towards the child node
     * @return A {@link NodeAndStack}, or empty when corresponding child is not found.
     * @throws NullPointerException if {@code path} is null
     */
    public @NonNull CheckedValue<@NonNull NodeAndStack, @NonNull IllegalArgumentException> enterPath(
            final YangInstanceIdentifier path) {
        final var stack = SchemaInferenceStack.of((EffectiveModelContext) root.dataSchemaNode());
        DataSchemaContext node = root;
        for (var arg : path.getPathArguments()) {
            final var child = node instanceof Composite composite ? composite.enterChild(stack, arg) : null;
            if (child == null) {
                return CheckedValue.ofException(new IllegalArgumentException("Failed to find " + arg + " in " + node));
            }
            node = child;
        }

        return CheckedValue.ofValue(new NodeAndStack(node, stack));
    }

    public DataSchemaContext.@NonNull Composite getRoot() {
        return root;
    }
}
