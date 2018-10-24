/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;

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

public interface StmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {

    @NonNull StatementSource getStatementSource();

    @NonNull StatementSourceReference getStatementSourceReference();

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

    /**
     * Return the {@link SchemaPath} of this statement. Not all statements have a SchemaPath, in which case
     * {@link Optional#empty()} is returned.
     *
     * @return Optional SchemaPath
     */
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
    @NonNull <K, V, T extends K, N extends IdentifierNamespace<K, V>> V getFromNamespace(Class<N> type, T key);

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

    Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement();

    CopyHistory getCopyHistory();

    boolean isSupportedByFeatures();

    Optional<StmtContext<?, ?, ?>> getOriginalCtx();

    ModelProcessingPhase getCompletedPhase();

    /**
     * Return version of root statement context.
     *
     * @return version of root statement context
     */
    @NonNull YangVersion getRootVersion();

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
        <K, V, T extends K, U extends V, N extends IdentifierNamespace<K, V>> void addToNs(Class<N> type, T key,
                U value);

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
        <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Mutable<X, Y, Z> childCopyOf(
                StmtContext<X, Y, Z> stmt, CopyType type, @Nullable QNameModule targetModule);

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
        default <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Mutable<X, Y, Z> childCopyOf(
                final StmtContext<X, Y, Z> stmt, final CopyType type) {
            return childCopyOf(stmt, type, null);
        }

        @NonNull Collection<? extends Mutable<?, ?, ?>> mutableDeclaredSubstatements();

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
        <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(Class<N> namespace, KT key,
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

        // FIXME: this seems to be unused, but looks useful.
        void setCompletedPhase(ModelProcessingPhase completedPhase);
    }
}
