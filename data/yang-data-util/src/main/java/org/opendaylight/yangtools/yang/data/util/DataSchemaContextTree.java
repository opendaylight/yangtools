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
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.CheckedValue;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.data.util.context.ContainerContext;
import org.opendaylight.yangtools.yang.data.util.context.YangDataContext;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
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

    /**
     * A step towards the {@code data tree} {@link DataSchemaNode} child.
     *
     * @since 16.2.0
     */
    // TODO: JEP-401: as close as possible to 'abstract value record'
    @NonNullByDefault
    public sealed interface Step {
        /**
         * {@return the child {@link DataSchemaContext}}
         */
        DataSchemaContext child();

        /**
         * A {@link Step} exactly matching a {@code schema tree} child.
         *
         * @param child child {@link DataSchemaContext}
         * @since 16.2.0
         */
        record Exact(DataSchemaContext child) implements Step {
            public Exact {
                requireNonNull(child);
            }
        }

        /**
         * A {@link Step} from a {@code choice} to a {@code data tree} child of a {@code case}. .
         *
         * @param child child {@link DataSchemaContext}
         * @since 16.2.0
         */
        record InCase(DataSchemaContext child, CaseEffectiveStatement inCase) implements Mixin {
            public InCase {
                requireNonNull(child);
                requireNonNull(inCase);
            }
        }

        /**
         * A {@link Step} matching a child entry of a {@code leaf-list} or a {@code list}.
         *
         * @param child child {@link DataSchemaContext}
         * @since 16.2.0
         */
        record OfEntry(DataSchemaContext child) implements Mixin {
            public OfEntry {
                requireNonNull(child);
            }
        }

        /**
         * A {@link Step} originating from a {@link DataSchemaContext.PathMixin}.
         *
         * @since 16.2.0
         */
        sealed interface Mixin extends Step {
            // nothing else
        }
    }

    private static final LoadingCache<EffectiveModelContext, @NonNull DataSchemaContextTree> TREES =
        CacheBuilder.newBuilder().weakKeys().weakValues().build(new CacheLoader<>() {
            @Override
            public DataSchemaContextTree load(final EffectiveModelContext key) {
                return new DataSchemaContextTree(key);
            }
        });

    // FIXME: ImmutableMap with compare-and-swap updates
    private final @NonNull ConcurrentHashMap<YangDataName, YangDataContext> yangData = new ConcurrentHashMap<>();
    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull ContainerContext root;

    private DataSchemaContextTree(final EffectiveModelContext modelContext) {
        this.modelContext = requireNonNull(modelContext);
        root = new ContainerContext(modelContext);
    }

    public static @NonNull DataSchemaContextTree from(final @NonNull EffectiveModelContext ctx) {
        return TREES.getUnchecked(ctx);
    }

    /**
     * Return the {@link EffectiveModelContext} used to derive this tree.
     *
     * @return the {@link EffectiveModelContext} used to derive this tree
     */
    public @NonNull EffectiveModelContext modelContext() {
        return modelContext;
    }

    /**
     * {@return the next {@link Step} towards the specified child node, or {@code null} if no such node exists}
     *
     * @param child data tree child name
     */
    public Step.@Nullable Exact stepTo(final @NonNull QName child) {
        return root.stepTo(child);
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
     * Find a child node as identified by {@link YangDataName}.
     *
     * @param name the {@link YangDataName}
     * @return child node if present, or {@code null} when corresponding child is not found.
     * @throws NullPointerException if {@code name} is {@code null}
     * @since 14.0.21
     */
    public DataSchemaContext.@Nullable Composite childYangData(final @NonNull YangDataName name) {
        final var existing = yangData.get(requireNonNull(name));
        return existing != null ? existing : loadYangData(name);

    }

    // Split out to aid inlining
    private DataSchemaContext.@Nullable Composite loadYangData(final @NonNull YangDataName name) {
        final var optModule = modelContext.findModuleStatement(name.module());
        if (optModule.isEmpty()) {
            return null;
        }

        for (var stmt : optModule.orElseThrow().filterEffectiveStatements(YangDataEffectiveStatement.class)) {
            if (name.equals(stmt.argument())) {
                final var created = new YangDataContext(stmt);
                final var raced = yangData.putIfAbsent(name, created);
                return raced != null ? raced : created;
            }
        }

        return null;
    }

    /**
     * Find a child node as identified by {@link YangDataName}.
     *
     * @param name the {@link YangDataName}
     * @return child node if present, or empty when corresponding child is not found.
     * @throws NullPointerException if {@code name} is {@code null}
     * @since 14.0.21
     */
    public @NonNull Optional<DataSchemaContext.@NonNull Composite> findYangData(final @NonNull YangDataName name) {
        // Optional.ofNullable() inline due to annotations
        final var child = childYangData(name);
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
        final var stack = SchemaInferenceStack.of(modelContext);
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
