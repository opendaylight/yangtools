/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * An inference context associated with an instance of a statement.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public interface StmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends NamespaceStmtCtx, BoundStmtCtxCompat<A, D> {
    /**
     * Return the parent statement context, or null if this is the root statement.
     *
     * @return context of parent of statement, or null if this is the root statement.
     */
    @Nullable StmtContext<?, ?, ?> getParentContext();

    /**
     * Return the parent statement context, forcing a VerifyException if this is the root statement.
     *
     * @return context of parent of statement
     * @throws VerifyException if this statement is the root statement
     */
    default @NonNull StmtContext<?, ?, ?> coerceParentContext() {
        return verifyNotNull(getParentContext(), "Root context %s does not have a parent", this);
    }

    /**
     * Returns the model root for this statement.
     *
     * @return root context of statement
     */
    @NonNull RootStmtContext<?, ?, ?> getRoot();

    /**
     * Return declared substatements. These are the statements which are explicitly written in the source model, but
     * reflect implicit containment statements as well. To but that statement into practical terms, this snippet:
     * <pre>
     *   <code>
     *     choice foo {
     *       container bar;
     *     }
     *   </code>
     * </pre>
     * reports the same structure as the canonical verbose equivalent:
     * <pre>
     *   <code>
     *     choice foo {
     *       case bar {
     *         container bar;
     *       }
     *     }
     *   </code>
     * </pre>
     * Returned collection is therefore well suited for reasoning about the schema tree. It is not appropriate for
     * populating populating {@link DeclaredStatement#declaredSubstatements()}.
     *
     * @return Collection of declared substatements
     */
    @NonNull Collection<? extends @NonNull StmtContext<?, ?, ?>> declaredSubstatements();

    /**
     * Return effective substatements. These are the statements which are added as this statement's substatements
     * complete their effective model phase.
     *
     * @return Collection of declared substatements
     */
    @NonNull Collection<? extends @NonNull StmtContext<?, ?, ?>> effectiveSubstatements();

    default Iterable<? extends @NonNull StmtContext<?, ?, ?>> allSubstatements() {
        return Iterables.concat(declaredSubstatements(), effectiveSubstatements());
    }

    default Stream<? extends @NonNull StmtContext<?, ?, ?>> allSubstatementsStream() {
        return Streams.concat(declaredSubstatements().stream(), effectiveSubstatements().stream());
    }

    /**
     * Return the {@link EffectiveStatement} for statement context, creating it if required. Implementations of this
     * method are required to memoize the returned object, so that subsequent invocation return the same object.
     *
     * <p>
     * If {@link #isSupportedToBuildEffective()} returns {@code false}, this method's behaviour is undefined.
     *
     * @return Effective statement instance.
     */
    @NonNull E buildEffective();

    boolean isSupportedToBuildEffective();

    boolean isSupportedByFeatures();

    Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement();

    /*
     * FIXME: YANGTOOLS-784: the next three methods are closely related to the copy process:
     *        - copyHistory() is a brief summary of what went on
     *        - getOriginalContext() points to the CopyHistory.ORIGINAL
     *        - getPreviousCopyCtx() points to the immediate predecessor forming a singly-linked list terminated
     *          at getOriginalContext()
     *
     *        When implementing YANGTOOLS-784, this needs to be taken into account and properly forwarded through
     *        intermediate MutableTrees. Also note this closely relates to current namespace context, as taken into
     *        account when creating the argument. At least parts of this are only needed during buildEffective()
     *        and hence should become arguments to that method.
     */

    /**
     * Return the statement context of the original definition, if this statement is an instantiated copy.
     *
     * @return Original definition, if this statement was copied.
     */
    Optional<StmtContext<A, D, E>> getOriginalCtx();

    /**
     * Return the context of the previous copy of this statement -- effectively walking towards the source origin
     * of this statement.
     *
     * @return Context of the previous copy of this statement, if this statement has been copied.
     */
    Optional<StmtContext<A, D, E>> getPreviousCopyCtx();

    /**
     * Create a replica of this statement as a substatement of specified {@code parent}. The replica must not be
     * modified and acts as a source of {@link EffectiveStatement} from outside of {@code parent}'s subtree.
     *
     * @param parent Parent of the replica statement
     * @return replica of this statement
     * @throws IllegalArgumentException if this statement cannot be replicated into parent, for example because it
     *                                  comes from an alien implementation.
     */
    @NonNull Mutable<A, D, E> replicaAsChildOf(Mutable<?, ?, ?> parent);

    @Beta
    @NonNull Optional<Mutable<A, D, E>> copyAsChildOf(Mutable<?, ?, ?> parent, CopyType type,
            @Nullable QNameModule targetModule);

    ModelProcessingPhase getCompletedPhase();

    /**
     * An mutable view of an inference context associated with an instance of a statement.
     *
     * @param <A> Argument type
     * @param <D> Declared Statement representation
     * @param <E> Effective Statement representation
     */
    interface Mutable<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            extends StmtContext<A, D, E> {

        @Override
        Mutable<?, ?, ?> getParentContext();

        @Override
        default Mutable<?, ?, ?> coerceParentContext() {
            return verifyNotNull(getParentContext(), "Root context %s does not have a parent", this);
        }

        /**
         * Associate a value with a key within a namespace.
         *
         * @param <K> key type
         * @param <V> value type
         * @param type Namespace type
         * @param key Key
         * @param value value
         * @throws NamespaceNotAvailableException when the namespace is not available.
         */
        <K, V> void addToNs(@NonNull ParserNamespace<K, V> type, K key, V value);

        @Override
        RootStmtContext.Mutable<?, ?, ?> getRoot();

        /**
         * Create a child sub-statement, which is a child of this statement, inheriting all attributes from specified
         * child and recording copy type. Resulting object may only be added as a child of this statement.
         *
         * @param stmt Statement to be used as a template
         * @param type Type of copy to record in history
         * @param targetModule Optional new target module
         * @return copy of statement considering {@link CopyType} (augment, uses)
         *
         * @throws IllegalArgumentException if stmt cannot be copied into this statement, for example because it comes
         *                                  from an alien implementation.
         * @throws org.opendaylight.yangtools.yang.parser.spi.source.SourceException instance of SourceException
         */
        Mutable<?, ?, ?> childCopyOf(StmtContext<?, ?, ?> stmt, CopyType type, @Nullable QNameModule targetModule);

        /**
         * Create a child sub-statement, which is a child of this statement, inheriting all attributes from specified
         * child and recording copy type. Resulting object may only be added as a child of this statement.
         *
         * @param stmt Statement to be used as a template
         * @param type Type of copy to record in history
         * @return copy of statement considering {@link CopyType} (augment, uses)
         *
         * @throws IllegalArgumentException if stmt cannot be copied into this statement, for example because it comes
         *                                  from an alien implementation.
         * @throws org.opendaylight.yangtools.yang.parser.spi.source.SourceException instance of SourceException
         */
        default Mutable<?, ?, ?> childCopyOf(final StmtContext<?, ?, ?> stmt, final CopyType type) {
            return childCopyOf(stmt, type, null);
        }

        @Override
        default Collection<? extends @NonNull StmtContext<?, ?, ?>> declaredSubstatements() {
            return mutableDeclaredSubstatements();
        }

        @NonNull Collection<? extends @NonNull Mutable<?, ?, ?>> mutableDeclaredSubstatements();

        @Override
        default Collection<? extends @NonNull StmtContext<?, ?, ?>> effectiveSubstatements() {
            return mutableEffectiveSubstatements();
        }

        @NonNull Collection<? extends @NonNull Mutable<?, ?, ?>> mutableEffectiveSubstatements();

        /**
         * Create a new inference action to be executed during specified phase. The action cannot be cancelled
         * and will be executed even if its definition remains incomplete. The specified phase cannot complete until
         * this action is resolved. If the action cannot be resolved, model processing will fail.
         *
         * @param phase Target phase in which the action will resolved.
         * @return A new action builder.
         * @throws NullPointerException if the specified phase is null
         */
        @NonNull ModelActionBuilder newInferenceAction(@NonNull ModelProcessingPhase phase);

        /**
         * Set version of root statement context.
         *
         * @param version
         *            of root statement context
         */
        void setRootVersion(YangVersion version);

        /**
         * Add required module. Based on these dependencies are collected required sources from library sources.
         *
         * @param dependency
         *            SourceIdentifier of module required by current root
         *            context
         */
        /*
         * FIXME: this method is used solely during SOURCE_PRE_LINKAGE reactor phase and does not have a corresponding
         *        getter -- which makes it rather strange. At some point this method needs to be deprecated and its
         *        users migrated to use proper global namespace.
         */
        void addRequiredSource(SourceIdentifier dependency);

        /**
         * Adds an effective statement to collection of substatements.
         *
         * @param substatement substatement
         * @throws IllegalStateException if added in declared phase
         * @throws NullPointerException if {@code substatement} is null
         */
        void addEffectiveSubstatement(Mutable<?, ?, ?> substatement);

        /**
         * Adds an effective statement to collection of substatements.
         *
         * @param statements substatements
         * @throws IllegalStateException if added in declared phase
         * @throws NullPointerException if statement parameter is null
         */
        void addEffectiveSubstatements(Collection<? extends Mutable<?, ?, ?>> statements);

        /**
         * Create a purely-effective substatement. The statement will report a {@code null}
         * {@link EffectiveStatement#getDeclared()} object. A typical example of statements which require this mechanics
         * are {@code rpc} and {@code action} statements, which always have {@code input} and {@code output}
         * substatements, even if those are not declared in YANG text. The returned context is not added to this
         * context's substatements. That needs to done once the statement is completely defined through
         * {@link #addEffectiveSubstatement(Mutable)} -- which will trigger
         * {@link StatementSupport#onFullDefinitionDeclared(Mutable)}.
         *
         * @param support Statement support of the statement being created
         * @param arg Effective argument. If specified as {@code null}, statement support will be consulted for the
         *            empty argument.
         * @return A new statement
         * @throws IllegalArgumentException if {@code support} does not implement {@link UndeclaredStatementFactory}
         * @throws IllegalStateException if added in declared phase
         * @throws NullPointerException if {@code support} is null
         */
        @Beta
        <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            @NonNull Mutable<X, Y, Z> createUndeclaredSubstatement(StatementSupport<X, Y, Z> support, @Nullable X arg);

        @Beta
        void removeStatementFromEffectiveSubstatements(StatementDefinition statementDef);

        /**
         * Removes a statement context from the effective substatements based on its statement definition (i.e statement
         * keyword) and raw (in String form) statement argument. The statement context is removed only if both statement
         * definition and statement argument match with one of the effective substatements' statement definition
         * and argument.
         *
         * <p>
         * If the statementArg parameter is null, the statement context is removed based only on its statement
         * definition.
         *
         * @param statementDef statement definition of the statement context to remove
         * @param statementArg statement argument of the statement context to remove
         */
        @Beta
        void removeStatementFromEffectiveSubstatements(StatementDefinition statementDef, String statementArg);

        @Beta
        // FIXME: this information should be exposed as a well-known Namespace
        boolean hasImplicitParentSupport();

        @Beta
        StmtContext<?, ?, ?> wrapWithImplicit(StmtContext<?, ?, ?> original);

        void addAsEffectOfStatement(Collection<? extends StmtContext<?, ?, ?>> ctxs);

        /**
         * Set identifier of current root context.
         *
         * @param identifier
         *            of current root context, must not be null
         */
        void setRootIdentifier(SourceIdentifier identifier);

        void setUnsupported();
    }
}
