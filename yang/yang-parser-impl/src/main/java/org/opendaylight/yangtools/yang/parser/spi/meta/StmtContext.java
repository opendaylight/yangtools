/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nullable
    StmtContext<?, ?, ?> getParentContext();

    @Nullable
    String rawStatementArgument();

    @Nullable
    A getStatementArgument();

    @Nullable
    List<Object> getArgumentsFromRoot();

    List<StmtContext<?,?,?>> getStmtContextsFromRoot();

    @Nonnull
    <K, V, KT extends K, N extends IdentifierNamespace<K, V>> V getFromNamespace(
            Class<N> type, KT key) throws NamespaceNotAvailableException;

    <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromNamespace(
            Class<N> type);

    <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(Class<N> type);

    @Nonnull
    StmtContext<?, ?, ?> getRoot();

    @Nonnull
    Collection<StatementContextBase<?, ?, ?>> declaredSubstatements();

    Collection<StatementContextBase<?, ?, ?>> effectiveSubstatements();

    D buildDeclared();

    E buildEffective();

    boolean isSupportedToBuildEffective();

    void setIsSupportedToBuildEffective(boolean isSupportedToBuild);

    Collection<StatementContextBase<?, ?, ?>> getEffectOfStatement();

    void addAsEffectOfStatement(StatementContextBase<?, ?, ?> ctx);

    StatementContextBase<?, ?, ?> createCopy(
            StatementContextBase<?, ?, ?> newParent, TypeOfCopy typeOfCopy)
            throws SourceException;

    StatementContextBase<?, ?, ?> createCopy(QNameModule newQNameModule,
            StatementContextBase<?, ?, ?> newParent, TypeOfCopy typeOfCopy)
            throws SourceException;

    enum TypeOfCopy {
        ORIGINAL, ADDED_BY_USES, ADDED_BY_AUGMENTATION, ADDED_BY_USES_AUGMENTATION
    }

    TypeOfCopy getTypeOfCopy();

    void setTypeOfCopy(TypeOfCopy typeOfCopy);

    StatementContextBase<?, ?, ?> getOriginalCtx();

    void setOriginalCtx(StatementContextBase<?, ?, ?> originalCtx);

    boolean isRootContext();

    void setCompletedPhase(ModelProcessingPhase completedPhase);

    ModelProcessingPhase getCompletedPhase();

    interface Mutable<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            extends StmtContext<A, D, E> {

        @Override
        StmtContext.Mutable<?, ?, ?> getParentContext();

        <K, V, KT extends K, VT extends V, N extends IdentifierNamespace<K, V>> void addToNs(
                Class<N> type, KT key, VT value)
                throws NamespaceNotAvailableException;

        @Override
        StmtContext.Mutable<?, ?, ?> getRoot();

        ModelActionBuilder newInferenceAction(ModelProcessingPhase phase);

        <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(
                Class<N> namespace, KT key, StmtContext<?, ?, ?> stmt);

    }

}
