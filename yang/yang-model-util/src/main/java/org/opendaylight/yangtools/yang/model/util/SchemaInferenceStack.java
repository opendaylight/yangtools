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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class SchemaInferenceStack implements Mutable {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaInferenceStack.class);

    private final Deque<EffectiveStatement<QName, ?>> deque = new ArrayDeque<>();
    private final EffectiveModelContext effectiveModel;

    private @Nullable ModuleEffectiveStatement currentModule;
    private int groupingDepth;

    public SchemaInferenceStack(final EffectiveModelContext effectiveModel) {
        this.effectiveModel = requireNonNull(effectiveModel);
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

    public void pushGrouping(final QName nodeIdentifier) {
        enterGrouping(requireNonNull(nodeIdentifier));
    }

    public void pushSchemaTree(final QName nodeIdentifier) {
        enterSchema(requireNonNull(nodeIdentifier));
    }

    public void pop() {
        final EffectiveStatement<?, ?> prev = deque.pop();
        if (prev instanceof GroupingEffectiveStatement) {
            --groupingDepth;
        }
        if (deque.isEmpty()) {
            currentModule = null;
        }
    }

    public Absolute toSchemaNodeIdentifier() {
        checkState(inInstantiatedContext(), "Cannot convert uninstantiated context %s", this);
        final List<QName> nodeIdentifiers = new ArrayList<>(deque.size());
        deque.descendingIterator().forEachRemaining(stmt -> nodeIdentifiers.add(stmt.argument()));
        return Absolute.of(nodeIdentifiers);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("stack", deque).toString();
    }

    private void enterGrouping(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<QName, ?> parent = deque.peekFirst();
        if (parent != null) {
            enterGrouping(parent, nodeIdentifier);
        } else {
            enterFirstGrouping(nodeIdentifier);
        }
    }

    private void enterGrouping(final @NonNull EffectiveStatement<?, ?> parent, final @NonNull QName nodeIdentifier) {
        final GroupingEffectiveStatement grp = parent.streamEffectiveSubstatements(GroupingEffectiveStatement.class)
            .filter(stmt -> nodeIdentifier.equals(stmt.argument()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Grouping " + nodeIdentifier + " not present"));
        deque.push(grp);
        ++groupingDepth;
    }

    private void enterFirstGrouping(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        enterGrouping(module, nodeIdentifier);
        currentModule = module;
    }

    private void enterSchema(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<QName, ?> parent = deque.peekFirst();
        if (parent != null) {
            enterSchema(parent, nodeIdentifier);
        } else {
            enterFirstSchema(nodeIdentifier);
        }
    }

    private void enterSchema(final EffectiveStatement<QName, ?> parent, final @NonNull QName nodeIdentifier) {
        checkState(parent instanceof SchemaTreeAwareEffectiveStatement, "Cannot descend schema tree at %s", parent);
        enterSchema((SchemaTreeAwareEffectiveStatement<?, ?>) parent, nodeIdentifier);
    }

    private void enterSchema(final @NonNull SchemaTreeAwareEffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        deque.push(parent.findSchemaTreeNode(nodeIdentifier).orElseThrow(
            () -> new IllegalArgumentException("Schema tree child " + nodeIdentifier + " not present")));
    }

    private void enterFirstSchema(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        enterSchema(module, nodeIdentifier);
        currentModule = module;
    }

    private static <T> @NonNull T checkNonNullState(final @Nullable T obj) {
        if (obj == null) {
            throw new IllegalStateException("Cannot execute on empty stack");
        }
        return obj;
    }

    private @NonNull ModuleEffectiveStatement getModule(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = effectiveModel.getModuleStatements().get(nodeIdentifier.getModule());
        checkArgument(module != null, "Module for %s not found", nodeIdentifier);
        return module;
    }
}
