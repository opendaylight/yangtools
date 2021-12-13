/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.UndeclaredCurrent;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.UndeclaredStatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
/**
 * Core reactor statement implementation of {@link Mutable}.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
final class UndeclaredStmtCtx<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends AbstractOriginalStmtCtx<A, D, E> implements UndeclaredCurrent<A, D> {
    private final StatementContextBase<?, ?, ?> parent;
    private final A argument;

    private UndeclaredStmtCtx(final UndeclaredStmtCtx<A, D, E> original, final StatementContextBase<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent);
        this.argument = original.argument;
    }

    UndeclaredStmtCtx(final StatementContextBase<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final @Nullable A argument) {
        super(def, ref);
        this.parent = requireNonNull(parent);
        this.argument = argument != null ? argument : def.parseArgumentValue(this, null);
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return ImmutableList.of();
    }

    @Override
    Stream<? extends @NonNull StmtContext<?, ?, ?>> streamDeclared() {
        return Stream.empty();
    }

    @Override
    void dropDeclaredSubstatements() {
        // No-op
    }

    @Override
    UndeclaredStmtCtx<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        return new UndeclaredStmtCtx<>(this, newParent);
    }

    @Override
    @SuppressWarnings("unchecked")
    E createEffective(final StatementFactory<A, D, E> factory) {
        verify(factory instanceof UndeclaredStatementFactory, "Unexpected factory %s", factory);
        return ((UndeclaredStatementFactory<A, D, E>) factory).createUndeclaredEffective(this, streamEffective());
    }

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in InferredStatementContext. If any adjustment is made here, make sure it is
     * properly updated there.
     */
    @Override
    public A argument() {
        return argument;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.STATEMENT_LOCAL;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentNamespaceStorage() {
        return parent;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    public EffectiveConfig effectiveConfig() {
        return effectiveConfig(parent);
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        return isIgnoringIfFeatures(parent);
    }

    @Override
    protected boolean isIgnoringConfig() {
        return isIgnoringConfig(parent);
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }
}
