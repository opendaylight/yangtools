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
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * A state tracking utility for walking {@link EffectiveModelContext}'s contents along schema/grouping namespaces. This
 * is conceptually a stack, tracking {@link EffectiveStatement}s encountered along traversal.
 *
 * <p>
 * This is meant to be a replacement concept for the use of {@link SchemaPath} in various places, notably
 * in {@link SchemaContextUtil} methods.
 *
 * <p>
 * This class is designed for single-threaded uses and does not make any guarantees around concurrent access.
 */
@Beta
public final class SchemaInferenceStack implements Mutable, EffectiveModelContextProvider {
    private final ArrayDeque<EffectiveStatement<QName, ?>> deque;
    private final @NonNull EffectiveModelContext effectiveModel;

    private @Nullable ModuleEffectiveStatement currentModule;
    private int groupingDepth;

    // True if there were only steps along grouping and schema tree, hence it is consistent with SchemaNodeIdentifier
    // False if we have evidence of a data tree lookup succeeding
    private boolean clean;

    private SchemaInferenceStack(final SchemaInferenceStack source) {
        this.deque = source.deque.clone();
        this.effectiveModel = source.effectiveModel;
        this.currentModule = source.currentModule;
        this.groupingDepth = source.groupingDepth;
        this.clean = source.clean;
    }

    private SchemaInferenceStack(final EffectiveModelContext effectiveModel, final int expectedSize) {
        this.deque = new ArrayDeque<>(expectedSize);
        this.effectiveModel = requireNonNull(effectiveModel);
        this.clean = true;
    }

    /**
     * Create a new empty stack backed by an effective model.
     *
     * @param effectiveModel EffectiveModelContext to which this stack is attached
     * @throws NullPointerException {@code effectiveModel} is null
     */
    public SchemaInferenceStack(final EffectiveModelContext effectiveModel) {
        this.deque = new ArrayDeque<>();
        this.effectiveModel = requireNonNull(effectiveModel);
        this.clean = true;
    }

    /**
     * Create a new stack backed by an effective model, pointing to specified schema node identified by
     * {@link Absolute}.
     *
     * @param effectiveModel EffectiveModelContext to which this stack is attached
     * @throws NullPointerException {@code effectiveModel} is null
     * @throws IllegalArgumentException if {@code path} cannot be resolved in the effective model
     */
    public static @NonNull SchemaInferenceStack of(final EffectiveModelContext effectiveModel, final Absolute path) {
        final SchemaInferenceStack ret = new SchemaInferenceStack(effectiveModel);
        path.getNodeIdentifiers().forEach(ret::enterSchemaTree);
        return ret;
    }

    /**
     * Create a new stack backed by an effective model, pointing to specified schema node identified by an absolute
     * {@link SchemaPath} and its {@link SchemaPath#getPathFromRoot()}.
     *
     * @param effectiveModel EffectiveModelContext to which this stack is attached
     * @throws NullPointerException {@code effectiveModel} is null
     * @throws IllegalArgumentException if {@code path} cannot be resolved in the effective model or if it is not an
     *                                  absolute path.
     */
    // FIXME: 7.0.0: consider deprecating this method
    public static @NonNull SchemaInferenceStack ofInstantiatedPath(final EffectiveModelContext effectiveModel,
            final SchemaPath path) {
        checkArgument(path.isAbsolute(), "Cannot operate on relative path %s", path);
        final SchemaInferenceStack ret = new SchemaInferenceStack(effectiveModel);
        path.getPathFromRoot().forEach(ret::enterSchemaTree);
        return ret;
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return effectiveModel;
    }

    /**
     * Create a deep copy of this object.
     *
     * @return An isolated copy of this object
     */
    public @NonNull SchemaInferenceStack copy() {
        return new SchemaInferenceStack(this);
    }

    /**
     * Check if this stack is empty.
     *
     * @return True if this stack has not entered any node.
     */
    public boolean isEmpty() {
        return deque.isEmpty();
    }

    /**
     * Return the statement at the top of the stack.
     *
     * @return Top statement
     * @throws IllegalStateException if the stack is empty
     */
    public @NonNull EffectiveStatement<QName, ?> currentStatement() {
        return checkNonNullState(deque.peekFirst());
    }

    /**
     * Return current module the stack has entered.
     *
     * @return Current module
     * @throws IllegalStateException if the stack is empty
     */
    public @NonNull ModuleEffectiveStatement currentModule() {
        return checkNonNullState(currentModule);
    }

    /**
     * Check if the stack is in instantiated context. This indicates the stack is non-empty and there is no grouping
     * (or similar construct) present in the stack.
     *
     * @return False if the stack is empty or contains a grouping, true otherwise.
     */
    public boolean inInstantiatedContext() {
        return groupingDepth == 0 && !deque.isEmpty();
    }

    /**
     * Reset this stack to empty state.
     */
    public void clear() {
        deque.clear();
        currentModule = null;
        groupingDepth = 0;
        clean = true;
    }

    /**
     * Lookup a {@code grouping} by its node identifier and push it to the stack.
     *
     * @param nodeIdentifier Node identifier of the grouping to enter
     * @return Resolved grouping
     * @throws NullPointerException if {@code nodeIdentifier} is null
     * @throws IllegalArgumentException if the corresponding grouping cannot be found
     */
    public @NonNull GroupingEffectiveStatement enterGrouping(final QName nodeIdentifier) {
        return pushGrouping(requireNonNull(nodeIdentifier));
    }

    /**
     * Lookup a {@code schema tree} child by its node identifier and push it to the stack.
     *
     * @param nodeIdentifier Node identifier of the schema tree child to enter
     * @return Resolved schema tree child
     * @throws NullPointerException if {@code nodeIdentifier} is null
     * @throws IllegalArgumentException if the corresponding child cannot be found
     */
    public @NonNull SchemaTreeEffectiveStatement<?> enterSchemaTree(final QName nodeIdentifier) {
        return pushSchema(requireNonNull(nodeIdentifier));
    }

    /**
     * Lookup a {@code schema tree} child by its node identifier and push it to the stack.
     *
     * @param nodeIdentifier Node identifier of the date tree child to enter
     * @return Resolved date tree child
     * @throws NullPointerException if {@code nodeIdentifier} is null
     * @throws IllegalArgumentException if the corresponding child cannot be found
     */
    public @NonNull DataTreeEffectiveStatement<?> enterDataTree(final QName nodeIdentifier) {
        return pushData(requireNonNull(nodeIdentifier));
    }

    /**
     * Pop the current statement from the stack.
     *
     * @return Previous statement
     * @throws NoSuchElementException if this stack is empty
     */
    public @NonNull EffectiveStatement<QName, ?> exit() {
        final EffectiveStatement<QName, ?> prev = deque.pop();
        if (prev instanceof GroupingEffectiveStatement) {
            --groupingDepth;
        }
        if (deque.isEmpty()) {
            currentModule = null;
            clean = true;
        }
        return prev;
    }

    /**
     * Convert current state into an absolute schema node identifier.
     *
     * @return Absolute schema node identifier representing current state
     * @throws IllegalStateException if current state is not instantiated
     */
    public @NonNull Absolute toSchemaNodeIdentifier() {
        checkState(inInstantiatedContext(), "Cannot convert uninstantiated context %s", this);
        final ImmutableList.Builder<QName> builder = ImmutableList.builderWithExpectedSize(deque.size());
        simplePathFromRoot().forEachRemaining(stmt -> builder.add(stmt.argument()));
        return Absolute.of(builder.build());
    }

    /**
     * Convert current state into a SchemaPath.
     *
     * @return Absolute SchemaPath representing current state
     * @throws IllegalStateException if current state is not instantiated
     * @deprecated This method is meant only for interoperation with SchemaPath-based APIs.
     */
    @Deprecated
    public @NonNull SchemaPath toSchemaPath() {
        SchemaPath ret = SchemaPath.ROOT;
        final Iterator<EffectiveStatement<QName, ?>> it = simplePathFromRoot();
        while (it.hasNext()) {
            ret = ret.createChild(it.next().argument());
        }
        return ret;
    }

    /**
     * Return an iterator along {@link SchemaPath#getPathFromRoot()}. This method is a faster equivalent of
     * {@code toSchemaPath().getPathFromRoot().iterator()}.
     *
     * @return An unmodifiable iterator
     */
    @Deprecated
    public @NonNull Iterator<QName> schemaPathIterator() {
        return Iterators.unmodifiableIterator(Iterators.transform(deque.descendingIterator(),
            EffectiveStatement::argument));
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

    private @NonNull SchemaTreeEffectiveStatement<?> pushSchema(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<QName, ?> parent = deque.peekFirst();
        return parent != null ? pushSchema(parent, nodeIdentifier) : pushFirstSchema(nodeIdentifier);
    }

    private @NonNull SchemaTreeEffectiveStatement<?> pushSchema(final EffectiveStatement<QName, ?> parent,
            final @NonNull QName nodeIdentifier) {
        checkState(parent instanceof SchemaTreeAwareEffectiveStatement, "Cannot descend schema tree at %s", parent);
        return pushSchema((SchemaTreeAwareEffectiveStatement<?, ?>) parent, nodeIdentifier);
    }

    private @NonNull SchemaTreeEffectiveStatement<?> pushSchema(
            final @NonNull SchemaTreeAwareEffectiveStatement<?, ?> parent, final @NonNull QName nodeIdentifier) {
        final SchemaTreeEffectiveStatement<?> ret = parent.findSchemaTreeNode(nodeIdentifier).orElseThrow(
            () -> new IllegalArgumentException("Schema tree child " + nodeIdentifier + " not present"));
        deque.push(ret);
        return ret;
    }

    private @NonNull SchemaTreeEffectiveStatement<?> pushFirstSchema(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        final SchemaTreeEffectiveStatement<?> ret = pushSchema(module, nodeIdentifier);
        currentModule = module;
        return ret;
    }

    private @NonNull DataTreeEffectiveStatement<?> pushData(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<QName, ?> parent = deque.peekFirst();
        return parent != null ? pushData(parent, nodeIdentifier) : pushFirstData(nodeIdentifier);
    }

    private @NonNull DataTreeEffectiveStatement<?> pushData(final EffectiveStatement<QName, ?> parent,
            final @NonNull QName nodeIdentifier) {
        checkState(parent instanceof DataTreeAwareEffectiveStatement, "Cannot descend data tree at %s", parent);
        return pushData((DataTreeAwareEffectiveStatement<?, ?>) parent, nodeIdentifier);
    }

    private @NonNull DataTreeEffectiveStatement<?> pushData(final @NonNull DataTreeAwareEffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        final DataTreeEffectiveStatement<?> ret = parent.findDataTreeNode(nodeIdentifier).orElseThrow(
            () -> new IllegalArgumentException("Schema tree child " + nodeIdentifier + " not present"));
        deque.push(ret);
        clean = false;
        return ret;
    }

    private @NonNull DataTreeEffectiveStatement<?> pushFirstData(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        final DataTreeEffectiveStatement<?> ret = pushData(module, nodeIdentifier);
        currentModule = module;
        return ret;
    }

    private @NonNull ModuleEffectiveStatement getModule(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = effectiveModel.getModuleStatements().get(nodeIdentifier.getModule());
        checkArgument(module != null, "Module for %s not found", nodeIdentifier);
        return module;
    }

    // Unified access to queue iteration for addressing purposes. Since we keep 'logical' steps as executed by user
    // at this point, conversion to SchemaNodeIdentifier may be needed. We dispatch based on 'clean'.
    private Iterator<EffectiveStatement<QName, ?>> simplePathFromRoot() {
        return clean ? deque.descendingIterator() : reconstructQNames();
    }

    // So there are some data tree steps in the stack... we essentially need to convert a data tree item into a series
    // of schema tree items. This means at least N searches, but after they are done, we get an opportunity to set the
    // clean flag.
    private Iterator<EffectiveStatement<QName, ?>> reconstructQNames() {
        // Let's walk all statements and decipher them into a temporary stack
        final SchemaInferenceStack tmp = new SchemaInferenceStack(effectiveModel, deque.size());
        final Iterator<EffectiveStatement<QName, ?>> it = deque.descendingIterator();
        while (it.hasNext()) {
            final EffectiveStatement<QName, ?> stmt = it.next();
            // Order of checks is significant
            if (stmt instanceof DataTreeEffectiveStatement) {
                tmp.resolveSchemaTreeSteps(stmt.argument());
            } else if (stmt instanceof SchemaTreeEffectiveStatement) {
                tmp.enterSchemaTree(stmt.argument());
            } else if (stmt instanceof GroupingEffectiveStatement) {
                tmp.enterGrouping(stmt.argument());
            } else {
                throw new VerifyException("Unexpected statement " + stmt);
            }
        }

        // if the sizes match, we did not jump through hoops. let's remember that for future.
        clean = deque.size() == tmp.deque.size();
        return tmp.deque.descendingIterator();
    }

    private void resolveSchemaTreeSteps(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<?, ?> parent = deque.peekFirst();
        if (parent != null) {
            verify(parent instanceof SchemaTreeAwareEffectiveStatement, "Unexpected parent %s", parent);
            resolveSchemaTreeSteps((SchemaTreeAwareEffectiveStatement<?, ?>)parent, nodeIdentifier);
            return;
        }

        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        resolveSchemaTreeSteps(module, nodeIdentifier);
        currentModule = module;
    }

    private void resolveSchemaTreeSteps(final @NonNull SchemaTreeAwareEffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        // The algebra of identifiers in 'schema tree versus data tree':
        // - data tree parents are always schema tree parents
        // - data tree children are always schema tree children

        // that implies that a data tree parent must satisfy schema tree queries with data tree children,
        // so a successful lookup of 'data tree parent -> child' and 'schema tree parent -> child' has to be the same
        // for a direct lookup.
        final SchemaTreeEffectiveStatement<?> found = parent.findSchemaTreeNode(nodeIdentifier).orElse(null);
        if (found instanceof DataTreeEffectiveStatement) {
            // ... and it did, we are done
            deque.push(found);
            return;
        }

        // Alright, so now it's down to filtering choice/case statements. For that we keep some globally-reused state
        // and employ a recursive match.
        final Deque<EffectiveStatement<QName, ?>> match = new ArrayDeque<>();
        for (EffectiveStatement<?, ?> stmt : parent.effectiveSubstatements()) {
            if (stmt instanceof ChoiceEffectiveStatement
                && searchChoice(match, (ChoiceEffectiveStatement) stmt, nodeIdentifier)) {
                match.descendingIterator().forEachRemaining(deque::push);
                return;
            }
        }

        throw new VerifyException("Failed to resolve " + nodeIdentifier + " in " + parent);
    }

    private static boolean searchCase(final @NonNull Deque<EffectiveStatement<QName, ?>> result,
            final @NonNull CaseEffectiveStatement parent, final @NonNull QName nodeIdentifier) {
        result.push(parent);
        for (EffectiveStatement<?, ?> stmt : parent.effectiveSubstatements()) {
            if (stmt instanceof DataTreeEffectiveStatement && nodeIdentifier.equals(stmt.argument())) {
                result.push((DataTreeEffectiveStatement<?>) stmt);
                return true;
            }
            if (stmt instanceof ChoiceEffectiveStatement
                && searchChoice(result, (ChoiceEffectiveStatement) stmt, nodeIdentifier)) {
                return true;
            }
        }
        result.pop();
        return false;
    }

    private static boolean searchChoice(final @NonNull Deque<EffectiveStatement<QName, ?>> result,
            final @NonNull ChoiceEffectiveStatement parent, final @NonNull QName nodeIdentifier) {
        result.push(parent);
        for (EffectiveStatement<?, ?> stmt : parent.effectiveSubstatements()) {
            if (stmt instanceof CaseEffectiveStatement
                && searchCase(result, (CaseEffectiveStatement) stmt, nodeIdentifier)) {
                return true;
            }
        }
        result.pop();
        return false;
    }

    private static <T> @NonNull T checkNonNullState(final @Nullable T obj) {
        if (obj == null) {
            throw new IllegalStateException("Cannot execute on empty stack");
        }
        return obj;
    }
}
