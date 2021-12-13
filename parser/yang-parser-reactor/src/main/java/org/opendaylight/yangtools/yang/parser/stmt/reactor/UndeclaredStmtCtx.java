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
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.UndeclaredCurrent;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.UndeclaredStatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;

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

    UndeclaredStmtCtx(final StatementContextBase<?, ?, ?> parent, final StatementSupport<A, D, E> support,
            final @Nullable A argument) {
        super(new StatementDefinitionContext<>(support), ImplicitSubstatement.of(parent.sourceReference()));
        this.parent = requireNonNull(parent);
        this.argument = argument != null ? argument : definition().parseArgumentValue(this, null);
    }

    // Exposed for StatementContextBase.wrapWithImplicit()
    UndeclaredStmtCtx(final StatementContextBase<?, ?, ?> original, final StatementSupport<A, D, E> support) {
        super(new StatementDefinitionContext<>(verifySupport(support)), original.sourceReference(),
            original.getLastOperation());
        this.parent = original.getParentContext();
        this.argument = castArgument(original);
    }

    // Exposed for implicit substatement wrapping in StatementContextBase.childCopyOf()
    UndeclaredStmtCtx(final StatementContextBase<?, ?, ?> parent, final StatementSupport<A, D, E> support,
            final StatementContextBase<?, ?, ?> original, final CopyType type) {
        super(new StatementDefinitionContext<>(verifySupport(support)), original.sourceReference(), type);
        this.parent = requireNonNull(parent);
        this.argument = castArgument(original);
    }

    // FIXME: this assumes original's argument type matches this type... which is true for the only case we
    //        currently care about (implicit case in choice, which is always triggered by a SchemaTree original),
    //        but this will need re-visiting
    @SuppressWarnings("unchecked")
    private static <A> @NonNull A castArgument(final StatementContextBase<?, ?, ?> original) {
        return (A) original.getArgument();
    }

    private static <T> T verifySupport(final T support) {
        verify(support instanceof UndeclaredStatementFactory, "Unexpected statement support %s", support);
        return support;
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
