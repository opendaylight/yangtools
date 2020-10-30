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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * An inference context associated with an instance of a statement.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public interface StmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {
    /**
     * Returns the origin of the statement.
     *
     * @return origin of statement
     */
    @NonNull StatementSource getStatementSource();

    /**
     * Returns a reference to statement source.
     *
     * @return reference of statement source
     */
    @NonNull StatementSourceReference getStatementSourceReference();

    /**
     * See {@link StatementSupport#getPublicView()}.
     */
    @NonNull StatementDefinition getPublicDefinition();

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
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string, or null if this statement does not have an argument.
     */
    @Nullable String rawStatementArgument();

    /**
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string
     * @throws VerifyException if this statement does not have an argument
     */
    default @NonNull String coerceRawStatementArgument() {
        return verifyNotNull(rawStatementArgument(), "Statement context %s does not have an argument", this);
    }

    /**
     * Return the statement argument.
     *
     * @return statement argument, or null if this statement does not have an argument
     */
    @Nullable A getStatementArgument();

    /**
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string
     * @throws VerifyException if this statement does not have an argument
     */
    default @NonNull A coerceStatementArgument() {
        return verifyNotNull(getStatementArgument(), "Statement context %s does not have an argument", this);
    }

    default <X, Y extends DeclaredStatement<X>> boolean producesDeclared(final Class<? super Y> type) {
        return type.isAssignableFrom(getPublicDefinition().getDeclaredRepresentationClass());
    }

    default <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<A, D>> boolean producesEffective(
            final Class<? super Z> type) {
        return type.isAssignableFrom(getPublicDefinition().getEffectiveRepresentationClass());
    }

    /**
     * Return the {@link SchemaPath} of this statement. Not all statements have a SchemaPath, in which case
     * {@link Optional#empty()} is returned.
     *
     * @return Optional SchemaPath
     * @deprecated Use of SchemaPath in the context of effective statements is going away. Consider not providing this
     *             information, if your users can exist without it.
     */
    @Deprecated
    @NonNull Optional<SchemaPath> getSchemaPath();

    boolean isConfiguration();

    boolean isEnabledSemanticVersioning();

    /**
     * Return a value associated with specified key within a namespace.
     *
     * @param type Namespace type
     * @param key Key
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param <N> namespace type
     * @param <T> key type
     * @return Value, or null if there is no element
     * @throws NamespaceNotAvailableException when the namespace is not available.
     */
    <K, V, T extends K, N extends IdentifierNamespace<K, V>> @Nullable V getFromNamespace(Class<@NonNull N> type,
            T key);

    <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromNamespace(Class<N> type);

    <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(Class<N> type);

    @NonNull StmtContext<?, ?, ?> getRoot();

    /**
     * Return declared substatements. These are the statements which are explicitly written in the source model.
     *
     * @return Collection of declared substatements
     */
    @NonNull Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements();

    /**
     * Return effective substatements. These are the statements which are added as this statement's substatements
     * complete their effective model phase.
     *
     * @return Collection of declared substatements
     */
    @NonNull Collection<? extends StmtContext<?, ?, ?>> effectiveSubstatements();

    default Iterable<? extends StmtContext<?, ?, ?>> allSubstatements() {
        return Iterables.concat(declaredSubstatements(), effectiveSubstatements());
    }

    default Stream<? extends StmtContext<?, ?, ?>> allSubstatementsStream() {
        return Streams.concat(declaredSubstatements().stream(), effectiveSubstatements().stream());
    }

    /**
     * Builds {@link DeclaredStatement} for statement context.
     */
    D buildDeclared();

    /**
     * Builds {@link EffectiveStatement} for statement context.
     */
    E buildEffective();

    boolean isSupportedToBuildEffective();

    boolean isSupportedByFeatures();

    Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement();

    /*
     * FIXME: YANGTOOLS-784: the next three methods are closely related to the copy process:
     *        - getCopyHistory() is a brief summary of what went on
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
     * Return the executive summary of the copy process that has produced this context.
     *
     * @return A simplified summary of the copy process.
     */
    CopyHistory getCopyHistory();

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

    ModelProcessingPhase getCompletedPhase();

    /**
     * Return version of root statement context.
     *
     * @return version of root statement context
     */
    @NonNull YangVersion getRootVersion();

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
         * @param type Namespace type
         * @param key Key
         * @param value value
         * @param <K> namespace key type
         * @param <V> namespace value type
         * @param <N> namespace type
         * @param <T> key type
         * @param <U> value type
         * @throws NamespaceNotAvailableException when the namespace is not available.
         */
        <K, V, T extends K, U extends V, N extends IdentifierNamespace<K, V>> void addToNs(Class<@NonNull N> type,
                T key, U value);

        @Override
        Mutable<?, ?, ?> getRoot();

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

        @Beta
        @NonNull Optional<? extends Mutable<?, ?, ?>> copyAsChildOf(Mutable<?, ?, ?> parent, CopyType type,
                @Nullable QNameModule targetModule);

        @Override
        default Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements() {
            return mutableDeclaredSubstatements();
        }

        @NonNull Collection<? extends Mutable<?, ?, ?>> mutableDeclaredSubstatements();

        @Override
        default Collection<? extends StmtContext<?, ?, ?>> effectiveSubstatements() {
            return mutableEffectiveSubstatements();
        }

        @NonNull Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements();

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
         * Adds s statement to namespace map with a key.
         *
         * @param namespace
         *            {@link StatementNamespace} child that determines namespace to be added to
         * @param key
         *            of type according to namespace class specification
         * @param stmt
         *            to be added to namespace map
         */
        <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(Class<@NonNull N> namespace, KT key,
                StmtContext<?, ?, ?> stmt);

        /**
         * Set version of root statement context.
         *
         * @param version
         *            of root statement context
         */
        void setRootVersion(YangVersion version);

        /**
         * Add mutable statement to seal. Each mutable statement must be sealed
         * as the last step of statement parser processing.
         *
         * @param mutableStatement
         *            mutable statement which should be sealed
         */
        void addMutableStmtToSeal(MutableStatement mutableStatement);

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

        void addAsEffectOfStatement(StmtContext<?, ?, ?> ctx);

        void addAsEffectOfStatement(Collection<? extends StmtContext<?, ?, ?>> ctxs);

        /**
         * Set identifier of current root context.
         *
         * @param identifier
         *            of current root context, must not be null
         */
        void setRootIdentifier(SourceIdentifier identifier);

        void setIsSupportedToBuildEffective(boolean isSupportedToBuild);
    }

    /**
     * Search of any child statement context of specified type and return its argument. If such a statement exists, it
     * is assumed to have the right argument. Users should be careful to use this method for statements which have
     * cardinality {@code 0..1}, otherwise this method can return any one of the statement's argument.
     *
     * <p>
     * The default implementation defers to
     * {@link StmtContextUtils#defaultFindSubstatementArgument(StmtContext, Class)}, subclasses are expected to provide
     * optimized implementation if possible.
     *
     * @param <X> Substatement argument type
     * @param <Z> Substatement effective statement representation
     * @param type Effective statement representation being look up
     * @return {@link Optional#empty()} if no statement exists, otherwise the argument value
     */
    default <X, Z extends EffectiveStatement<X, ?>> @NonNull Optional<X> findSubstatementArgument(
            final @NonNull Class<Z> type) {
        return StmtContextUtils.defaultFindSubstatementArgument(this, type);
    }

    /**
     * Check if there is any child statement context of specified type.
     *
     * <p>
     * The default implementation defers to {@link StmtContextUtils#defaultHasSubstatement(StmtContext, Class)},
     * subclasses are expected to provide optimized implementation if possible.
     *
     * @param type Effective statement representation being look up
     * @return True if such a child statement exists, false otherwise
     */
    default boolean hasSubstatement(final @NonNull Class<? extends EffectiveStatement<?, ?>> type) {
        return StmtContextUtils.defaultHasSubstatement(this, type);
    }
}
