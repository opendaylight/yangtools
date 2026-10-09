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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.CheckedValue;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Choice;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.ListLike;
import org.opendaylight.yangtools.yang.data.util.context.CaseContext;
import org.opendaylight.yangtools.yang.data.util.context.ContainerContext;
import org.opendaylight.yangtools.yang.data.util.context.YangDataContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.meta.DataSchemaCompat;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
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

    /**
     * Find the context of the node an {@link EffectiveStatementInference} points to. An empty inference points to
     * {@link #getRoot()}.
     *
     * <p>Nodes in the data tree and in a {@code yang-data} come from this tree, so lookups through them are shared by
     * every user of this tree. An {@code rpc}, {@code action} or {@code notification} is not part of the data tree: the
     * contexts at and below it are created for this call only. The same holds for an inference ending at a
     * {@code case}: its context holds only that case's children and is created for this call only.
     *
     * @param inference the inference, which has to come from this tree's model context
     * @return the context, or {@code null} if the inference ends at a {@code choice}, or passes through a statement
     *         that has no context
     * @throws NullPointerException if {@code inference} is {@code null}
     * @throws IllegalArgumentException if {@code inference} comes from a different model context
     */
    public @Nullable DataSchemaContext childByInference(final @NonNull EffectiveStatementInference inference) {
        if (!modelContext.equals(inference.modelContext())) {
            throw new IllegalArgumentException("Mismatched inference: expecting model context " + modelContext
                + ", got " + inference.modelContext());
        }

        DataSchemaContext current = root;
        // Set while the inference stands at a choice or a case, so we know where it ended
        EffectiveStatement<?, ?> lastChoiceOrCase = null;
        for (var stmt : inference.statementPath()) {
            lastChoiceOrCase = null;
            switch (stmt) {
                case DataTreeEffectiveStatement<?> data -> current = childOf(current, data.argument());
                // A choice and its case have no context in the tree: the next child is found by its name alone
                case ChoiceEffectiveStatement choice -> lastChoiceOrCase = choice;
                case CaseEffectiveStatement caze -> lastChoiceOrCase = caze;
                case YangDataEffectiveStatement yangDataStmt -> current = childYangData(yangDataStmt.argument());
                case DataSchemaCompat<?, ?> compat -> current = DataSchemaContext.of(compat.toDataSchemaNode());
                default -> current = null;
            }
            if (current == null) {
                return null;
            }
        }

        return switch (lastChoiceOrCase) {
            case null -> current;
            // Ending at a case means the data holds just that case's children, so give them a context of their own
            case CaseEffectiveStatement caze -> new CaseContext(caze.toDataSchemaNode());
            // A choice holds only cases, never data nodes, so data cannot start there
            default -> null;
        };
    }

    private static @Nullable DataSchemaContext childOf(final DataSchemaContext parent, final QName qname) {
        // A list or leaf-list context stands for the whole list: its entries have a context of their own
        var current = parent instanceof ListLike list ? list.entry() : parent;
        if (!(current instanceof Composite composite)) {
            return null;
        }

        var child = composite.childByQName(qname);
        while (child instanceof Choice choice) {
            child = choice.childByQName(qname);
        }
        return child;
    }

    public DataSchemaContext.@NonNull Composite getRoot() {
        return root;
    }
}
