/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public interface StmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {

    @Nonnull
    StatementSource getStatementSource();

    @Nonnull
    StatementSourceReference getStatementSourceReference();

    @Nonnull
    StatementDefinition getPublicDefinition();

    @Nullable
    StmtContext<?, ?, ?> getParentContext();

    @Nullable
    String rawStatementArgument();

    @Nullable
    A getStatementArgument();

    /**
     * Return the {@link SchemaPath} of this statement. Not all statements have a SchemaPath, in which case
     * {@link Optional#absent()} is returned.
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
    Collection<StatementContextBase<?, ?, ?>> declaredSubstatements();

    /**
     * Return effective substatements. These are the statements which are added as this statement's substatements
     * complete their effective model phase.
     *
     * @return Collection of declared substatements
     */
    @Nonnull
    Collection<StatementContextBase<?, ?, ?>> effectiveSubstatements();

    D buildDeclared();

    E buildEffective();

    boolean isSupportedToBuildEffective();

    void setIsSupportedToBuildEffective(boolean isSupportedToBuild);

    Collection<StatementContextBase<?, ?, ?>> getEffectOfStatement();

    void addAsEffectOfStatement(StatementContextBase<?, ?, ?> ctx);

    void addAsEffectOfStatement(Collection<StatementContextBase<?, ?, ?>> ctxs);

    StatementContextBase<?, ?, ?> createCopy(
            StatementContextBase<?, ?, ?> newParent, CopyType typeOfCopy)
            throws SourceException;

    StatementContextBase<?, ?, ?> createCopy(QNameModule newQNameModule,
            StatementContextBase<?, ?, ?> newParent, CopyType typeOfCopy)
            throws SourceException;

    CopyHistory getCopyHistory();

    enum SupportedByFeatures {
        UNDEFINED, SUPPORTED, NOT_SUPPORTED
    }

    SupportedByFeatures getSupportedByFeatures();

    void appendCopyHistory(CopyType typeOfCopy, CopyHistory toAppend);

    StatementContextBase<?, ?, ?> getOriginalCtx();

    void setOriginalCtx(StatementContextBase<?, ?, ?> originalCtx);

    boolean isRootContext();

    void setOrder(int order);

    int getOrder();

    void setCompletedPhase(ModelProcessingPhase completedPhase);

    ModelProcessingPhase getCompletedPhase();

    interface Mutable<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            extends StmtContext<A, D, E> {

        @Override
        StmtContext.Mutable<?, ?, ?> getParentContext();

        <K, V, KT extends K, VT extends V, N extends IdentifierNamespace<K, V>> void addToNs(
                Class<N> type, KT key, VT value)
                throws NamespaceNotAvailableException;

        @Nonnull
        @Override
        StmtContext.Mutable<?, ?, ?> getRoot();

        ModelActionBuilder newInferenceAction(ModelProcessingPhase phase);

        <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(
                Class<N> namespace, KT key, StmtContext<?, ?, ?> stmt);

        void setSupportedByFeatures(boolean isSupported);
    }

}
