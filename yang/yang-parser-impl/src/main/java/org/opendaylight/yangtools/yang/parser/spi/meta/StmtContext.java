/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

public interface StmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {

    @Nonnull
    StatementSource getStatementSource();

    @Nonnull
    StatementSourceReference getStatementSourceReference();

    @Nonnull
    StatementDefinition getPublicDefinition();

    /**
     * Return the parent statement context, or null if this is the root statement.
     *
     * @return context of parent of statement, or null if this is the root statement.
     */
    @Nullable
    StmtContext<?, ?, ?> getParentContext();

    /**
     * @return raw statement argument string
     */
    @Nullable
    String rawStatementArgument();

    @Nullable
    A getStatementArgument();

    /**
     * Return the {@link SchemaPath} of this statement. Not all statements have a SchemaPath, in which case
     * {@link Optional#empty()} is returned.
     *
     * @return Optional SchemaPath
     */
    @Nonnull Optional<SchemaPath> getSchemaPath();

    boolean isConfiguration();

    boolean isEnabledSemanticVersioning();

    @Nonnull
    <K, V, KT extends K, N extends IdentifierNamespace<K, V>> V getFromNamespace(
            Class<N> type, KT key) throws NamespaceNotAvailableException;

    <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromNamespace(
            Class<N> type);

    <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(Class<N> type);

    @Nonnull
    StmtContext<?, ?, ?> getRoot();

    /**
     * Return declared substatements. These are the statements which are explicitly written in the source model.
     *
     * @return Collection of declared substatements
     */
    @Nonnull
    Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements();

    /**
     * Return effective substatements. These are the statements which are added as this statement's substatements
     * complete their effective model phase.
     *
     * @return Collection of declared substatements
     */
    @Nonnull
    Collection<? extends StmtContext<?, ?, ?>> effectiveSubstatements();

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
     * Builds {@link EffectiveStatement} for statement context
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
    @Nonnull YangVersion getRootVersion();

    interface Mutable<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            extends StmtContext<A, D, E> {

        @Override
        Mutable<?, ?, ?> getParentContext();

        <K, V, KT extends K, VT extends V, N extends IdentifierNamespace<K, V>> void addToNs(Class<N> type, KT key,
                VT value) throws NamespaceNotAvailableException;

        @Nonnull
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

        @Nonnull
        Collection<? extends Mutable<?, ?, ?>> mutableDeclaredSubstatements();

        @Nonnull
        Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements();

        /**
         * Create a new inference action to be executed during specified phase. The action cannot be cancelled
         * and will be executed even if its definition remains incomplete.
         *
         * @param phase Target phase in which the action will resolved.
         * @return A new action builder.
         * @throws NullPointerException if the specified phase is null
         */
        @Nonnull ModelActionBuilder newInferenceAction(@Nonnull ModelProcessingPhase phase);

        /**
         * adds statement to namespace map with the key
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
         * Add required module. Based on these dependencies are collected
         * required sources from library sources.
         *
         * @param dependency
         *            ModuleIdentifier of module required by current root
         *            context
         */
        void addRequiredModule(ModuleIdentifier dependency);

        void addAsEffectOfStatement(StmtContext<?, ?, ?> ctx);

        void addAsEffectOfStatement(Collection<? extends StmtContext<?, ?, ?>> ctxs);

        /**
         * Set identifier of current root context.
         *
         * @param identifier
         *            of current root context
         */
        void setRootIdentifier(ModuleIdentifier identifier);

        void setIsSupportedToBuildEffective(boolean isSupportedToBuild);

        // FIXME: this seems to be unused, but looks useful.
        void setCompletedPhase(ModelProcessingPhase completedPhase);
    }
}
