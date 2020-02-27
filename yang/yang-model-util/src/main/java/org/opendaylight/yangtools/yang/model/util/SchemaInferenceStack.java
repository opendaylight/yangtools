/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;

/**
 * A state tracking utility for walking {@link EffectiveModelContext}'s contents along schema/grouping namespaces. This
 * is conceptually a stack, tracking {@link EffectiveStatement}s encountered along traversal.
 *
 * <p>
 * This is mean to be a replacement concept for the use of {@link SchemaPath} in various places, notably
 * in {@link SchemaContextUtil} methods.
 */
@Beta
public final class SchemaInferenceStack implements Mutable, EffectiveModelContextProvider {
    private final ArrayDeque<EffectiveStatement<QName, ?>> deque;
    private final @NonNull EffectiveModelContext effectiveModel;

    private @Nullable ModuleEffectiveStatement currentModule;
    private int groupingDepth;

    private SchemaInferenceStack(final SchemaInferenceStack source) {
        this.deque = source.deque.clone();
        this.effectiveModel = source.effectiveModel;
        this.currentModule = source.currentModule;
        this.groupingDepth = source.groupingDepth;
    }

    public SchemaInferenceStack(final EffectiveModelContext effectiveModel) {
        this.deque = new ArrayDeque<>();
        this.effectiveModel = requireNonNull(effectiveModel);
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return effectiveModel;
    }

    public @NonNull SchemaInferenceStack copy() {
        return new SchemaInferenceStack(this);
    }

    public boolean isEmpty() {
        return deque.isEmpty();
    }

    public @NonNull EffectiveStatement<QName, ?> currentStatement() {
        return checkNonNullState(deque.peekFirst());
    }

    public @NonNull ModuleEffectiveStatement currentModule() {
        return checkNonNullState(currentModule);
    }

    public boolean inInstantiatedContext() {
        return groupingDepth != 0 && !deque.isEmpty();
    }

    public void clear() {
        deque.clear();
        currentModule = null;
        groupingDepth = 0;
    }

    public @NonNull GroupingEffectiveStatement enterGrouping(final QName nodeIdentifier) {
        return pushGrouping(requireNonNull(nodeIdentifier));
    }

    public @NonNull EffectiveStatement<QName, ?> enterSchemaTree(final QName nodeIdentifier) {
        return pushSchema(requireNonNull(nodeIdentifier));
    }

    public @NonNull EffectiveStatement<QName, ?> exit() {
        final EffectiveStatement<QName, ?> prev = deque.pop();
        if (prev instanceof GroupingEffectiveStatement) {
            --groupingDepth;
        }
        if (deque.isEmpty()) {
            currentModule = null;
        }
        return prev;
    }

    public @NonNull Absolute toSchemaNodeIdentifier() {
        checkState(inInstantiatedContext(), "Cannot convert uninstantiated context %s", this);
        final ImmutableList.Builder<QName> builder = ImmutableList.builderWithExpectedSize(deque.size());
        deque.descendingIterator().forEachRemaining(stmt -> builder.add(stmt.argument()));
        return Absolute.of(builder.build());
    }

    @Deprecated
    public @NonNull SchemaPath toSchemaPath() {
        SchemaPath ret = SchemaPath.ROOT;
        final Iterator<EffectiveStatement<QName, ?>> it = deque.descendingIterator();
        while (it.hasNext()) {
            ret = ret.createChild(it.next().argument());
        }
        return ret;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("stack", deque).toString();
    }

    private @NonNull GroupingEffectiveStatement pushGrouping(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<QName, ?> parent = deque.peekFirst();
        return parent != null ? pushGrouping(parent, nodeIdentifier) : pushFirstGrouping(nodeIdentifier);
    }

    private @NonNull GroupingEffectiveStatement pushGrouping(final @NonNull EffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        final GroupingEffectiveStatement ret = parent.streamEffectiveSubstatements(GroupingEffectiveStatement.class)
            .filter(stmt -> nodeIdentifier.equals(stmt.argument()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Grouping " + nodeIdentifier + " not present"));
        deque.push(ret);
        ++groupingDepth;
        return ret;
    }

    private @NonNull GroupingEffectiveStatement pushFirstGrouping(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        final GroupingEffectiveStatement ret = pushGrouping(module, nodeIdentifier);
        currentModule = module;
        return ret;
    }

    private @NonNull EffectiveStatement<QName, ?> pushSchema(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<QName, ?> parent = deque.peekFirst();
        return parent != null ? pushSchema(parent, nodeIdentifier) : pushFirstSchema(nodeIdentifier);
    }

    private @NonNull EffectiveStatement<QName, ?> pushSchema(final EffectiveStatement<QName, ?> parent,
            final @NonNull QName nodeIdentifier) {
        checkState(parent instanceof SchemaTreeAwareEffectiveStatement, "Cannot descend schema tree at %s", parent);
        return pushSchema((SchemaTreeAwareEffectiveStatement<?, ?>) parent, nodeIdentifier);
    }

    private @NonNull EffectiveStatement<QName, ?> pushSchema(
            final @NonNull SchemaTreeAwareEffectiveStatement<?, ?> parent, final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<QName, ?> ret = parent.findSchemaTreeNode(nodeIdentifier).orElseThrow(
            () -> new IllegalArgumentException("Schema tree child " + nodeIdentifier + " not present"));
        deque.push(ret);
        return ret;
    }

    private @NonNull EffectiveStatement<QName, ?> pushFirstSchema(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        final EffectiveStatement<QName, ?> ret = pushSchema(module, nodeIdentifier);
        currentModule = module;
        return ret;
    }

    private @NonNull ModuleEffectiveStatement getModule(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = effectiveModel.getModuleStatements().get(nodeIdentifier.getModule());
        checkArgument(module != null, "Module for %s not found", nodeIdentifier);
        return module;
    }

    private static <T> @NonNull T checkNonNullState(final @Nullable T obj) {
        if (obj == null) {
            throw new IllegalStateException("Cannot execute on empty stack");
        }
        return obj;
    }
}
