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
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.DerefSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.Steps;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.spi.AbstractEffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.AxisStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.slf4j.LoggerFactory;

/**
 * A state tracking utility for walking {@link EffectiveModelContext}'s contents along schema/grouping namespaces. This
 * is conceptually a stack, tracking {@link EffectiveStatement}s encountered along traversal.
 *
 * <p>
 * This class is designed for single-threaded uses and does not make any guarantees around concurrent access.
 */
@Beta
public final class SchemaInferenceStack implements Mutable, EffectiveModelContextProvider, LeafrefResolver {
    /**
     * Semantic binding of {@link EffectiveStatementInference} produced by {@link SchemaInferenceStack}. Sequence of
     * {@link #statementPath()} is implementation-specific.
     */
    @Beta
    public static final class Inference extends AbstractEffectiveStatementInference<EffectiveStatement<?, ?>> {
        private final ArrayDeque<EffectiveStatement<?, ?>> deque;
        private final ModuleEffectiveStatement currentModule;
        private final int groupingDepth;
        private final boolean clean;

        Inference(final @NonNull EffectiveModelContext modelContext, final ArrayDeque<EffectiveStatement<?, ?>> deque,
                final ModuleEffectiveStatement currentModule, final int groupingDepth, final boolean clean) {
            super(modelContext);
            this.deque = requireNonNull(deque);
            this.currentModule = currentModule;
            this.groupingDepth = groupingDepth;
            this.clean = clean;
        }

        /**
         * Create a new stack backed by an effective model and set up to point and specified data tree node.
         *
         * @param effectiveModel EffectiveModelContext to which this stack is attached
         * @param qnames Data tree path qnames
         * @return A new stack
         * @throws NullPointerException if any argument is {@code null} or path contains a {@code null} element
         * @throws IllegalArgumentException if a path element cannot be found
         */
        public static @NonNull Inference ofDataTreePath(final EffectiveModelContext effectiveModel,
                final QName... qnames) {
            return SchemaInferenceStack.ofDataTreePath(effectiveModel, qnames).toInference();
        }

        @Override
        public List<EffectiveStatement<?, ?>> statementPath() {
            return ImmutableList.copyOf(deque);
        }

        /**
         * Return {@code true} if this inference is empty. This is a more efficient alternative to
         * {@code statementPath().isEmpty()}.
         *
         * @return {@code true} if {@link #statementPath()} returns an empty list
         */
        public boolean isEmpty() {
            return deque.isEmpty();
        }

        /**
         * Convert this inference into a {@link SchemaInferenceStack}.
         *
         * @return A new stack
         */
        public @NonNull SchemaInferenceStack toSchemaInferenceStack() {
            return new SchemaInferenceStack(getEffectiveModelContext(), deque, currentModule, groupingDepth, clean);
        }
    }

    private static final String VERIFY_DEFAULT_SCHEMA_TREE_INFERENCE_PROP =
        "org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.verifyDefaultSchemaTreeInference";
    private static final boolean VERIFY_DEFAULT_SCHEMA_TREE_INFERENCE =
        Boolean.getBoolean(VERIFY_DEFAULT_SCHEMA_TREE_INFERENCE_PROP);

    static {
        if (VERIFY_DEFAULT_SCHEMA_TREE_INFERENCE) {
            LoggerFactory.getLogger(SchemaInferenceStack.class)
                .info("SchemaTreeStack.ofInference(DefaultSchemaTreeInference) argument is being verified");
        }
    }

    private final @NonNull EffectiveModelContext effectiveModel;
    private final ArrayDeque<EffectiveStatement<?, ?>> deque;

    private @Nullable ModuleEffectiveStatement currentModule;
    private int groupingDepth;

    // True if there were only steps along grouping and schema tree, hence it is consistent with SchemaNodeIdentifier
    // False if we have evidence of a data tree lookup succeeding
    private boolean clean;

    private SchemaInferenceStack(final EffectiveModelContext effectiveModel, final int expectedSize) {
        deque = new ArrayDeque<>(expectedSize);
        this.effectiveModel = requireNonNull(effectiveModel);
        clean = true;
    }

    private SchemaInferenceStack(final SchemaInferenceStack source) {
        deque = source.deque.clone();
        effectiveModel = source.effectiveModel;
        currentModule = source.currentModule;
        groupingDepth = source.groupingDepth;
        clean = source.clean;
    }

    private SchemaInferenceStack(final EffectiveModelContext effectiveModel,
            final ArrayDeque<EffectiveStatement<?, ?>> deque, final ModuleEffectiveStatement currentModule,
            final int groupingDepth, final boolean clean) {
        this.effectiveModel = requireNonNull(effectiveModel);
        this.deque = deque.clone();
        this.currentModule = currentModule;
        this.groupingDepth = groupingDepth;
        this.clean = clean;
    }

    private SchemaInferenceStack(final EffectiveModelContext effectiveModel) {
        this.effectiveModel = requireNonNull(effectiveModel);
        deque = new ArrayDeque<>();
        clean = true;
    }

    /**
     * Create a new empty stack backed by an effective model.
     *
     * @param effectiveModel EffectiveModelContext to which this stack is attached
     * @return A new stack
     * @throws NullPointerException if {@code effectiveModel} is {@code null}
     */
    public static @NonNull SchemaInferenceStack of(final EffectiveModelContext effectiveModel) {
        return new SchemaInferenceStack(effectiveModel);
    }

    /**
     * Create a new stack backed by an effective model, pointing to specified schema node identified by
     * {@link Absolute}.
     *
     * @param effectiveModel EffectiveModelContext to which this stack is attached
     * @return A new stack
     * @throws NullPointerException if {@code effectiveModel} is {@code null}
     * @throws IllegalArgumentException if {@code path} cannot be resolved in the effective model
     */
    public static @NonNull SchemaInferenceStack of(final EffectiveModelContext effectiveModel, final Absolute path) {
        final SchemaInferenceStack ret = new SchemaInferenceStack(effectiveModel);
        path.getNodeIdentifiers().forEach(ret::enterSchemaTree);
        return ret;
    }

    /**
     * Create a new stack from an {@link EffectiveStatementInference}.
     *
     * @param inference Inference to use for initialization
     * @return A new stack
     * @throws NullPointerException if {@code inference} is {@code null}
     * @throws IllegalArgumentException if {@code inference} implementation is not supported
     */
    public static @NonNull SchemaInferenceStack ofInference(final EffectiveStatementInference inference) {
        if (inference.statementPath().isEmpty()) {
            return new SchemaInferenceStack(inference.getEffectiveModelContext());
        } else if (inference instanceof SchemaTreeInference sti) {
            return ofInference(sti);
        } else if (inference instanceof Inference inf) {
            return inf.toSchemaInferenceStack();
        } else {
            throw new IllegalArgumentException("Unsupported Inference " + inference);
        }
    }

    /**
     * Create a new stack from an {@link SchemaTreeInference}.
     *
     * @param inference SchemaTreeInference to use for initialization
     * @return A new stack
     * @throws NullPointerException if {@code inference} is {@code null}
     * @throws IllegalArgumentException if {@code inference} cannot be resolved to a valid stack
     */
    public static @NonNull SchemaInferenceStack ofInference(final SchemaTreeInference inference) {
        return inference instanceof DefaultSchemaTreeInference dsti ? ofInference(dsti)
            : of(inference.getEffectiveModelContext(), inference.toSchemaNodeIdentifier());
    }

    /**
     * Create a new stack from an {@link DefaultSchemaTreeInference}. The argument is nominally trusted to be an
     * accurate representation of the schema tree.
     *
     * <p>
     * Run-time verification of {@code inference} can be enabled by setting the
     * {@value #VERIFY_DEFAULT_SCHEMA_TREE_INFERENCE_PROP} system property to {@code true}.
     *
     * @param inference DefaultSchemaTreeInference to use for initialization
     * @return A new stack
     * @throws NullPointerException if {@code inference} is {@code null}
     * @throws IllegalArgumentException if {@code inference} refers to a missing module or when verification is enabled
     *                                  and it does not match its context's schema tree
     */
    public static @NonNull SchemaInferenceStack ofInference(final DefaultSchemaTreeInference inference) {
        return VERIFY_DEFAULT_SCHEMA_TREE_INFERENCE ? ofUntrusted(inference) : ofTrusted(inference);
    }

    private static @NonNull SchemaInferenceStack ofTrusted(final DefaultSchemaTreeInference inference) {
        final var path = inference.statementPath();
        final var ret = new SchemaInferenceStack(inference.getEffectiveModelContext(), path.size());
        ret.currentModule = ret.getModule(path.get(0).argument());
        ret.deque.addAll(path);
        return ret;
    }

    @VisibleForTesting
    static @NonNull SchemaInferenceStack ofUntrusted(final DefaultSchemaTreeInference inference) {
        final var ret = of(inference.getEffectiveModelContext(), inference.toSchemaNodeIdentifier());
        if (!Iterables.elementsEqual(ret.deque, inference.statementPath())) {
            throw new IllegalArgumentException("Provided " + inference + " is not consistent with resolved path "
                + ret.toSchemaTreeInference());
        }
        return ret;
    }

    /**
     * Create a new stack backed by an effective model and set up to point and specified data tree node.
     *
     * @param effectiveModel EffectiveModelContext to which this stack is attached
     * @return A new stack
     * @throws NullPointerException if any argument is {@code null} or path contains a {@code null} element
     * @throws IllegalArgumentException if a path element cannot be found
     */
    public static @NonNull SchemaInferenceStack ofDataTreePath(final EffectiveModelContext effectiveModel,
            final QName... path) {
        final SchemaInferenceStack ret = new SchemaInferenceStack(effectiveModel);
        for (QName qname : path) {
            ret.enterDataTree(qname);
        }
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
     * @return {@code true} if this stack has not entered any node
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
    public @NonNull EffectiveStatement<?, ?> currentStatement() {
        return checkNonNullState(deque.peekLast());
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
     * Check if the stack is in instantiated context. This indicates the stack is non-empty and there are only schema
     * tree statements in the stack.
     *
     * @return {@code false} if the stack is empty or contains a statement which is not a
     *         {@link SchemaTreeEffectiveStatement}, {@code true} otherwise.
     */
    public boolean inInstantiatedContext() {
        return groupingDepth == 0 && !deque.isEmpty()
            && deque.stream().allMatch(SchemaTreeEffectiveStatement.class::isInstance);
    }

    /**
     * Check if the stack is in a {@code grouping} context.
     *
     * @return {@code false} if the stack contains a grouping.
     */
    public boolean inGrouping() {
        return groupingDepth != 0;
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
     * Lookup a {@code choice} by its node identifier and push it to the stack. This step is very similar to
     * {@link #enterSchemaTree(QName)}, except it handles the use case where traversal ignores actual {@code case}
     * intermediate schema tree children.
     *
     * @param nodeIdentifier Node identifier of the choice to enter
     * @return Resolved choice
     * @throws NullPointerException if {@code nodeIdentifier} is {@code null}
     * @throws IllegalArgumentException if the corresponding choice cannot be found
     */
    public @NonNull ChoiceEffectiveStatement enterChoice(final QName nodeIdentifier) {
        final QName nodeId = requireNonNull(nodeIdentifier);
        final EffectiveStatement<?, ?> parent = deque.peekLast();
        if (parent instanceof ChoiceEffectiveStatement choice) {
            return enterChoice(choice, nodeId);
        }

        // Fall back to schema tree lookup. Note if it results in non-choice, we rewind before reporting an error
        final SchemaTreeEffectiveStatement<?> result = enterSchemaTree(nodeId);
        if (result instanceof ChoiceEffectiveStatement choice) {
            return choice;
        }
        exit();

        if (parent != null) {
            throw notPresent(parent, "Choice", nodeId);
        }
        throw new IllegalArgumentException("Choice " + nodeId + " not present");
    }

    // choice -> choice transition, we have to deal with intermediate case nodes
    private @NonNull ChoiceEffectiveStatement enterChoice(final @NonNull ChoiceEffectiveStatement parent,
            final QName nodeIdentifier) {
        for (EffectiveStatement<?, ?> stmt : parent.effectiveSubstatements()) {
            if (stmt instanceof CaseEffectiveStatement caze) {
                final Optional<ChoiceEffectiveStatement> optMatch = caze.findSchemaTreeNode(nodeIdentifier)
                    .filter(ChoiceEffectiveStatement.class::isInstance)
                    .map(ChoiceEffectiveStatement.class::cast);
                if (optMatch.isPresent()) {
                    final var match = optMatch.orElseThrow();
                    deque.addLast(match);
                    clean = false;
                    return match;
                }
            }
        }
        throw notPresent(parent, "Choice", nodeIdentifier);
    }

    /**
     * Lookup a {@code grouping} by its node identifier and push it to the stack.
     *
     * @param nodeIdentifier Node identifier of the grouping to enter
     * @return Resolved grouping
     * @throws NullPointerException if {@code nodeIdentifier} is {@code null}
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
     * @throws NullPointerException if {@code nodeIdentifier} is {@code null}
     * @throws IllegalArgumentException if the corresponding child cannot be found
     */
    public @NonNull SchemaTreeEffectiveStatement<?> enterSchemaTree(final QName nodeIdentifier) {
        return pushSchema(requireNonNull(nodeIdentifier));
    }

    /**
     * Lookup a {@code schema tree} node by its schema node identifier and push it to the stack.
     *
     * @param nodeIdentifier Schema node identifier of the schema tree node to enter
     * @return Resolved schema tree node
     * @throws NullPointerException if {@code nodeIdentifier} is {@code null}
     * @throws IllegalArgumentException if the corresponding node cannot be found
     */
    public @NonNull SchemaTreeEffectiveStatement<?> enterSchemaTree(final SchemaNodeIdentifier nodeIdentifier) {
        if (nodeIdentifier instanceof Absolute) {
            clear();
        }

        final Iterator<QName> it = nodeIdentifier.getNodeIdentifiers().iterator();
        SchemaTreeEffectiveStatement<?> ret;
        do {
            ret = enterSchemaTree(it.next());
        } while (it.hasNext());

        return ret;
    }

    /**
     * Lookup a {@code schema tree} child by its node identifier and push it to the stack.
     *
     * @param nodeIdentifier Node identifier of the date tree child to enter
     * @return Resolved date tree child
     * @throws NullPointerException if {@code nodeIdentifier} is {@code null}
     * @throws IllegalArgumentException if the corresponding child cannot be found
     */
    public @NonNull DataTreeEffectiveStatement<?> enterDataTree(final QName nodeIdentifier) {
        return pushData(requireNonNull(nodeIdentifier));
    }

    /**
     * Lookup a {@code typedef} by its node identifier and push it to the stack.
     *
     * @param nodeIdentifier Node identifier of the typedef to enter
     * @return Resolved typedef
     * @throws NullPointerException if {@code nodeIdentifier} is {@code null}
     * @throws IllegalArgumentException if the corresponding typedef cannot be found
     */
    public @NonNull TypedefEffectiveStatement enterTypedef(final QName nodeIdentifier) {
        return pushTypedef(requireNonNull(nodeIdentifier));
    }

    /**
     * Lookup a {@code rc:yang-data} by the module namespace where it is defined and its template name.
     *
     * @param namespace Module namespace in which to lookup the template
     * @param name Template name
     * @return Resolved yang-data
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if the corresponding yang-data cannot be found
     * @throws IllegalStateException if this stack is not empty
     */
    public @NonNull YangDataEffectiveStatement enterYangData(final QNameModule namespace, final String name) {
        final EffectiveStatement<?, ?> parent = deque.peekLast();
        checkState(parent == null, "Cannot lookup yang-data in a non-empty stack");

        final String templateName = requireNonNull(name);
        final ModuleEffectiveStatement module = effectiveModel.getModuleStatements().get(requireNonNull(namespace));
        checkArgument(module != null, "Module for %s not found", namespace);

        final YangDataEffectiveStatement ret = module.streamEffectiveSubstatements(YangDataEffectiveStatement.class)
            .filter(stmt -> templateName.equals(stmt.argument()))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("yang-data " + templateName + " not present in " + namespace));
        deque.addLast(ret);
        currentModule = module;
        return ret;
    }

    /**
     * Pop the current statement from the stack.
     *
     * @return Previous statement
     * @throws NoSuchElementException if this stack is empty
     */
    public @NonNull EffectiveStatement<?, ?> exit() {
        final EffectiveStatement<?, ?> prev = deque.removeLast();
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
     * Pop the current statement from the stack, asserting it is a {@link DataTreeEffectiveStatement} and that
     * subsequent {@link #enterDataTree(QName)} will find it again.
     *
     * @return Previous statement
     * @throws NoSuchElementException if this stack is empty
     * @throws IllegalStateException if current statement is not a DataTreeEffectiveStatement or if its parent is not
     *                               a {@link DataTreeAwareEffectiveStatement}
     */
    public @NonNull DataTreeEffectiveStatement<?> exitToDataTree() {
        final EffectiveStatement<?, ?> child = exit();
        checkState(child instanceof DataTreeEffectiveStatement, "Unexpected current %s", child);
        EffectiveStatement<?, ?> parent = deque.peekLast();
        while (parent instanceof ChoiceEffectiveStatement || parent instanceof CaseEffectiveStatement) {
            deque.pollLast();
            parent = deque.peekLast();
        }

        checkState(parent == null || parent instanceof DataTreeAwareEffectiveStatement, "Unexpected parent %s", parent);
        return (DataTreeEffectiveStatement<?>) child;
    }

    @Override
    public TypeDefinition<?> resolveLeafref(final LeafrefTypeDefinition type) {
        final SchemaInferenceStack tmp = copy();

        LeafrefTypeDefinition current = type;
        while (true) {
            final EffectiveStatement<?, ?> resolved = tmp.resolvePathExpression(current.getPathStatement());
            checkState(resolved instanceof TypeAware, "Unexpected result %s resultion of %s", resolved, type);
            final TypeDefinition<?> result = ((TypedDataSchemaNode) resolved).getType();
            if (result instanceof LeafrefTypeDefinition leafref) {
                checkArgument(result != type, "Resolution of %s loops back onto itself via %s", type, current);
                current = leafref;
            } else {
                return result;
            }
        }
    }

    /**
     * Resolve a {@link PathExpression}.
     *
     * <p>
     * Note if this method throws, this stack may be in an undefined state.
     *
     * @param path Requested path
     * @return Resolved schema tree child
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws IllegalArgumentException if the target node cannot be found
     * @throws VerifyException if path expression is invalid
     */
    public @NonNull EffectiveStatement<?, ?> resolvePathExpression(final PathExpression path) {
        final Steps steps = path.getSteps();
        if (steps instanceof LocationPathSteps location) {
            return resolveLocationPath(location.getLocationPath());
        } else if (steps instanceof DerefSteps deref) {
            return resolveDeref(deref);
        } else {
            throw new VerifyException("Unhandled steps " + steps);
        }
    }

    private @NonNull EffectiveStatement<?, ?> resolveDeref(final DerefSteps deref) {
        final EffectiveStatement<?, ?> leafRefSchemaNode = currentStatement();
        final YangLocationPath.Relative derefArg = deref.getDerefArgument();
        final EffectiveStatement<?, ?> derefStmt = resolveLocationPath(derefArg);
        checkArgument(derefStmt != null, "Cannot find deref(%s) target node %s in context of %s",
                derefArg, leafRefSchemaNode);
        checkArgument(derefStmt instanceof TypedDataSchemaNode, "deref(%s) resolved to non-typed %s", derefArg,
                derefStmt);

        // We have a deref() target, decide what to do about it
        final TypeDefinition<?> targetType = ((TypedDataSchemaNode) derefStmt).getType();
        if (targetType instanceof InstanceIdentifierTypeDefinition) {
            // Static inference breaks down, we cannot determine where this points to
            // FIXME: dedicated exception, users can recover from it, derive from IAE
            throw new UnsupportedOperationException("Cannot infer instance-identifier reference " + targetType);
        }

        // deref() is defined only for instance-identifier and leafref types, handle the latter
        checkArgument(targetType instanceof LeafrefTypeDefinition, "Illegal target type %s", targetType);

        final PathExpression dereferencedLeafRefPath = ((LeafrefTypeDefinition) targetType).getPathStatement();
        EffectiveStatement<?, ?> derefNode = resolvePathExpression(dereferencedLeafRefPath);
        checkArgument(derefStmt != null, "Can not find target node of dereferenced node %s", derefStmt);
        checkArgument(derefNode instanceof LeafSchemaNode, "Unexpected %s reference in %s", deref,
                dereferencedLeafRefPath);
        return resolveLocationPath(deref.getRelativePath());
    }

    private @NonNull EffectiveStatement<?, ?> resolveLocationPath(final YangLocationPath path) {
        // get the default namespace before we clear and loose our deque
        final QNameModule defaultNamespace = deque.isEmpty() ? null : ((QName) deque.peekLast().argument()).getModule();
        if (path.isAbsolute()) {
            clear();
        }

        EffectiveStatement<?, ?> current = null;
        for (Step step : path.getSteps()) {
            final YangXPathAxis axis = step.getAxis();
            switch (axis) {
                case PARENT -> {
                    verify(step instanceof AxisStep, "Unexpected parent step %s", step);
                    try {
                        current = exitToDataTree();
                    } catch (IllegalStateException | NoSuchElementException e) {
                        throw new IllegalArgumentException("Illegal parent access in " + path, e);
                    }
                }
                case CHILD -> {
                    verify(step instanceof QNameStep, "Unexpected child step %s", step);
                    current = enterChild((QNameStep) step, defaultNamespace);
                }
                default -> throw new VerifyException("Unexpected step " + step);
            }
        }

        return verifyNotNull(current);
    }

    private @NonNull EffectiveStatement<?, ?> enterChild(final QNameStep step, final QNameModule defaultNamespace) {
        final AbstractQName toResolve = step.getQName();
        final QName qname;
        if (toResolve instanceof QName) {
            qname = (QName) toResolve;
        } else if (toResolve instanceof Unqualified unqual) {
            checkArgument(defaultNamespace != null, "Can not find target module of step %s", step);
            qname = unqual.bindTo(defaultNamespace);
        } else {
            throw new VerifyException("Unexpected child step QName " + toResolve);
        }
        return enterDataTree(qname);
    }

    /**
     * Return an {@link Inference} equivalent of current state.
     *
     * @return An {@link Inference}
     */
    public @NonNull Inference toInference() {
        return new Inference(effectiveModel, deque.clone(), currentModule, groupingDepth, clean);
    }

    /**
     * Return an {@link SchemaTreeInference} equivalent of current state.
     *
     * @return An {@link SchemaTreeInference}
     * @throws IllegalStateException if current state cannot be converted to a {@link SchemaTreeInference}
     */
    public @NonNull SchemaTreeInference toSchemaTreeInference() {
        checkState(inInstantiatedContext(), "Cannot convert uninstantiated context %s", this);
        final var cleanDeque = clean ? deque : reconstructSchemaInferenceStack().deque;
        return DefaultSchemaTreeInference.unsafeOf(getEffectiveModelContext(), cleanDeque.stream()
            .map(stmt -> (SchemaTreeEffectiveStatement<?>) stmt)
            .collect(ImmutableList.toImmutableList()));
    }

    /**
     * Convert current state into an absolute schema node identifier.
     *
     * @return Absolute schema node identifier representing current state
     * @throws IllegalStateException if current state is not instantiated
     */
    public @NonNull Absolute toSchemaNodeIdentifier() {
        checkState(inInstantiatedContext(), "Cannot convert uninstantiated context %s", this);
        return Absolute.of(simplePathFromRoot());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("path", deque).toString();
    }

    private @NonNull GroupingEffectiveStatement pushGrouping(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<?, ?> parent = deque.peekLast();
        return parent != null ? pushGrouping(parent, nodeIdentifier) : pushFirstGrouping(nodeIdentifier);
    }

    private @NonNull GroupingEffectiveStatement pushGrouping(final @NonNull EffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        final GroupingEffectiveStatement ret = parent.streamEffectiveSubstatements(GroupingEffectiveStatement.class)
            .filter(stmt -> nodeIdentifier.equals(stmt.argument()))
            .findFirst()
            .orElseThrow(() -> notPresent(parent, "Grouping", nodeIdentifier));
        deque.addLast(ret);
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
        final EffectiveStatement<?, ?> parent = deque.peekLast();
        return parent != null ? pushSchema(parent, nodeIdentifier) : pushFirstSchema(nodeIdentifier);
    }

    private @NonNull SchemaTreeEffectiveStatement<?> pushSchema(final EffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        checkState(parent instanceof SchemaTreeAwareEffectiveStatement, "Cannot descend schema tree at %s", parent);
        return pushSchema((SchemaTreeAwareEffectiveStatement<?, ?>) parent, nodeIdentifier);
    }

    private @NonNull SchemaTreeEffectiveStatement<?> pushSchema(
            final @NonNull SchemaTreeAwareEffectiveStatement<?, ?> parent, final @NonNull QName nodeIdentifier) {
        final SchemaTreeEffectiveStatement<?> ret = parent.findSchemaTreeNode(nodeIdentifier)
            .orElseThrow(() -> notPresent(parent, "Schema tree child ", nodeIdentifier));
        deque.addLast(ret);
        return ret;
    }

    private @NonNull SchemaTreeEffectiveStatement<?> pushFirstSchema(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        final SchemaTreeEffectiveStatement<?> ret = pushSchema(module, nodeIdentifier);
        currentModule = module;
        return ret;
    }

    private @NonNull DataTreeEffectiveStatement<?> pushData(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<?, ?> parent = deque.peekLast();
        return parent != null ? pushData(parent, nodeIdentifier) : pushFirstData(nodeIdentifier);
    }

    private @NonNull DataTreeEffectiveStatement<?> pushData(final EffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        checkState(parent instanceof DataTreeAwareEffectiveStatement, "Cannot descend data tree at %s", parent);
        return pushData((DataTreeAwareEffectiveStatement<?, ?>) parent, nodeIdentifier);
    }

    private @NonNull DataTreeEffectiveStatement<?> pushData(final @NonNull DataTreeAwareEffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        final DataTreeEffectiveStatement<?> ret = parent.findDataTreeNode(nodeIdentifier)
            .orElseThrow(() -> notPresent(parent, "Data tree child", nodeIdentifier));
        deque.addLast(ret);
        clean = false;
        return ret;
    }

    private @NonNull DataTreeEffectiveStatement<?> pushFirstData(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        final DataTreeEffectiveStatement<?> ret = pushData(module, nodeIdentifier);
        currentModule = module;
        return ret;
    }

    private @NonNull TypedefEffectiveStatement pushTypedef(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<?, ?> parent = deque.peekLast();
        return parent != null ? pushTypedef(parent, nodeIdentifier) : pushFirstTypedef(nodeIdentifier);
    }

    private @NonNull TypedefEffectiveStatement pushTypedef(final @NonNull EffectiveStatement<?, ?> parent,
            final @NonNull QName nodeIdentifier) {
        if (parent instanceof TypedefAwareEffectiveStatement<?, ?> aware) {
            final TypedefEffectiveStatement ret = aware.findTypedef(nodeIdentifier)
                .orElseThrow(() -> notPresent(parent, "Typedef", nodeIdentifier));
            deque.addLast(ret);
            return ret;
        }
        throw notPresent(parent, "Typedef", nodeIdentifier);
    }

    private @NonNull TypedefEffectiveStatement pushFirstTypedef(final @NonNull QName nodeIdentifier) {
        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        final TypedefEffectiveStatement ret = pushTypedef(module, nodeIdentifier);
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
    private Collection<QName> simplePathFromRoot() {
        return clean ? qnames() : reconstructQNames();
    }

    private Collection<QName> qnames() {
        return Collections2.transform(deque, stmt -> {
            final Object argument = stmt.argument();
            verify(argument instanceof QName, "Unexpected statement %s", stmt);
            return (QName) argument;
        });
    }

    // So there are some data tree steps in the stack... we essentially need to convert a data tree item into a series
    // of schema tree items. This means at least N searches, but after they are done, we get an opportunity to set the
    // clean flag.
    private Collection<QName> reconstructQNames() {
        return reconstructSchemaInferenceStack().qnames();
    }

    private SchemaInferenceStack reconstructSchemaInferenceStack() {
        // Let's walk all statements and decipher them into a temporary stack
        final SchemaInferenceStack tmp = new SchemaInferenceStack(effectiveModel, deque.size());
        for (EffectiveStatement<?, ?> stmt : deque) {
            // Order of checks is significant
            if (stmt instanceof DataTreeEffectiveStatement<?> dataTree) {
                tmp.resolveDataTreeSteps(dataTree.argument());
            } else if (stmt instanceof ChoiceEffectiveStatement choice) {
                tmp.resolveChoiceSteps(choice.argument());
            } else if (stmt instanceof SchemaTreeEffectiveStatement<?> schemaTree) {
                tmp.enterSchemaTree(schemaTree.argument());
            } else if (stmt instanceof GroupingEffectiveStatement grouping) {
                tmp.enterGrouping(grouping.argument());
            } else if (stmt instanceof TypedefEffectiveStatement typedef) {
                tmp.enterTypedef(typedef.argument());
            } else {
                throw new VerifyException("Unexpected statement " + stmt);
            }
        }

        // if the sizes match, we did not jump through hoops. let's remember that for future.
        if (deque.size() == tmp.deque.size()) {
            clean = true;
            return this;
        }
        return tmp;
    }

    private void resolveChoiceSteps(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<?, ?> parent = deque.peekLast();
        if (parent instanceof ChoiceEffectiveStatement choice) {
            resolveChoiceSteps(choice, nodeIdentifier);
        } else {
            enterSchemaTree(nodeIdentifier);
        }
    }

    private void resolveChoiceSteps(final @NonNull ChoiceEffectiveStatement parent,
            final @NonNull QName nodeIdentifier) {
        for (EffectiveStatement<?, ?> stmt : parent.effectiveSubstatements()) {
            if (stmt instanceof CaseEffectiveStatement caze) {
                final SchemaTreeEffectiveStatement<?> found = caze.findSchemaTreeNode(nodeIdentifier).orElse(null);
                if (found instanceof ChoiceEffectiveStatement) {
                    deque.addLast(caze);
                    deque.addLast(found);
                    return;
                }
            }
        }
        throw new VerifyException("Failed to resolve " + nodeIdentifier + " in " + parent);
    }

    private void resolveDataTreeSteps(final @NonNull QName nodeIdentifier) {
        final EffectiveStatement<?, ?> parent = deque.peekLast();
        if (parent != null) {
            verify(parent instanceof SchemaTreeAwareEffectiveStatement, "Unexpected parent %s", parent);
            resolveDataTreeSteps((SchemaTreeAwareEffectiveStatement<?, ?>) parent, nodeIdentifier);
            return;
        }

        final ModuleEffectiveStatement module = getModule(nodeIdentifier);
        resolveDataTreeSteps(module, nodeIdentifier);
        currentModule = module;
    }

    private void resolveDataTreeSteps(final @NonNull SchemaTreeAwareEffectiveStatement<?, ?> parent,
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
            deque.addLast(found);
            return;
        }

        // Alright, so now it's down to filtering choice/case statements. For that we keep some globally-reused state
        // and employ a recursive match.
        final var match = new ArrayDeque<EffectiveStatement<QName, ?>>();
        for (EffectiveStatement<?, ?> stmt : parent.effectiveSubstatements()) {
            if (stmt instanceof ChoiceEffectiveStatement choice && searchChoice(match, choice, nodeIdentifier)) {
                deque.addAll(match);
                return;
            }
        }

        throw new VerifyException("Failed to resolve " + nodeIdentifier + " in " + parent);
    }

    private static boolean searchCase(final @NonNull ArrayDeque<EffectiveStatement<QName, ?>> result,
            final @NonNull CaseEffectiveStatement parent, final @NonNull QName nodeIdentifier) {
        result.addLast(parent);
        for (EffectiveStatement<?, ?> stmt : parent.effectiveSubstatements()) {
            if (stmt instanceof DataTreeEffectiveStatement<?> dataTree && nodeIdentifier.equals(stmt.argument())) {
                result.addLast(dataTree);
                return true;
            }
            if (stmt instanceof ChoiceEffectiveStatement choice && searchChoice(result, choice, nodeIdentifier)) {
                return true;
            }
        }
        result.removeLast();
        return false;
    }

    private static boolean searchChoice(final @NonNull ArrayDeque<EffectiveStatement<QName, ?>> result,
            final @NonNull ChoiceEffectiveStatement parent, final @NonNull QName nodeIdentifier) {
        result.addLast(parent);
        for (EffectiveStatement<?, ?> stmt : parent.effectiveSubstatements()) {
            if (stmt instanceof CaseEffectiveStatement caze && searchCase(result, caze, nodeIdentifier)) {
                return true;
            }
        }
        result.removeLast();
        return false;
    }

    private static <T> @NonNull T checkNonNullState(final @Nullable T obj) {
        if (obj == null) {
            throw new IllegalStateException("Cannot execute on empty stack");
        }
        return obj;
    }

    private static @NonNull IllegalArgumentException notPresent(final @NonNull EffectiveStatement<?, ?> parent,
            final @NonNull String name, final QName nodeIdentifier) {
        return new IllegalArgumentException(name + " " + nodeIdentifier + " not present in " + describeParent(parent));
    }

    private static @NonNull String describeParent(final @NonNull EffectiveStatement<?, ?> parent) {
        // Add just enough information to be useful without being overly-verbose. Note we want to expose namespace
        // information, so that we understand what revisions we are dealing with
        if (parent instanceof SchemaTreeEffectiveStatement) {
            return "schema parent " + parent.argument();
        } else if (parent instanceof GroupingEffectiveStatement) {
            return "grouping " + parent.argument();
        } else if (parent instanceof ModuleEffectiveStatement module) {
            return "module " + module.argument().bindTo(module.localQNameModule());
        } else {
            // Shorthand for QNames, should provide enough context
            final Object arg = parent.argument();
            return "parent " + (arg instanceof QName ? arg : parent);
        }
    }
}
