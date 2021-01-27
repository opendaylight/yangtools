/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import java.util.Collection;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

/**
 * Specialization of {@link AbstractQNameStatementSupport} for {@link SchemaTreeEffectiveStatement} implementations.
 * Every statement automatically participates in {@link SchemaTreeNamespace}.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class BaseSchemaTreeStatementSupport<D extends DeclaredStatement<QName>,
        E extends SchemaTreeEffectiveStatement<D>> extends AbstractQNameStatementSupport<D, E> {
    private static class SchemaTreeEquality<D extends DeclaredStatement<QName>>
            implements StatementEquality<QName, D> {
        private static final class Instantiated<D extends DeclaredStatement<QName>> extends SchemaTreeEquality<D> {
            @Override
            public boolean canReuseCurrent(final Current<QName, D> copy, final Current<QName, D> current,
                    final Collection<? extends EffectiveStatement<?, ?>> substatements) {
                return copy.effectiveConfig() == current.effectiveConfig()
                    && super.canReuseCurrent(copy, current, substatements)
                    // This weird quirk is needed for ... something somewhere
                    && Objects.equals(copy.original(), current.original());
            }
        }

        @Override
        public boolean canReuseCurrent(final Current<QName, D> copy, final Current<QName, D> current,
                final Collection<? extends EffectiveStatement<?, ?>> substatements) {
            return equalHistory(copy.history(), current.history())
                && copy.getArgument().equals(current.getArgument())
                // FIXME: 8.0.0: eliminate this call
                && copy.equalParentPath(current);
        }

        private static boolean equalHistory(final CopyHistory copy, final CopyHistory current) {
            return copy.isAugmenting() == current.isAugmenting() && copy.isAddedByUses() == current.isAddedByUses();
        }
    }

    private static final StatementPolicy<QName, ?> INSTANTIATED_POLICY =
        StatementPolicy.copyDeclared(new SchemaTreeEquality.Instantiated<>());
    private static final StatementPolicy<QName, ?> UNINSTANTIATED_POLICY =
        StatementPolicy.copyDeclared(new SchemaTreeEquality<>());

    protected BaseSchemaTreeStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<QName, D> policy) {
        super(publicDefinition, policy);
    }

    /**
     * Return the {@link StatementPolicy} corresponding to a potentially-instantiated YANG statement. Statements are
     * reused as long as:
     * <ul>
     *   <li>{@link Current#schemaPath()} does not change</li>
     *   <li>{@link Current#argument()} does not change</li>
     *   <li>{@link Current#history()} does not change as far as {@link CopyableNode} is concerned</li>
     *   <li>{@link Current#effectiveConfig()} does not change</li>
     *   <li>{@link Current#original()} does not change</li>
     * </ul>
     *
     * <p>
     * Typical users include {@code container} and {@code leaf}.
     *
     * @param <D> Declared Statement representation
     * @return A StatementPolicy
     */
    @SuppressWarnings("unchecked")
    public static final <D extends DeclaredStatement<QName>> StatementPolicy<QName, D> instantiatedPolicy() {
        return (StatementPolicy<QName, D>) INSTANTIATED_POLICY;
    }

    /**
     * Return the {@link StatementPolicy} corresponding to an uninstantiated YANG statement. Statements are
     * reused as long as:
     * <ul>
     *   <li>{@link Current#schemaPath()} does not change</li>
     *   <li>{@link Current#argument()} does not change</li>
     *   <li>{@link Current#history()} does not change as far as {@link CopyableNode} is concerned</li>
     * </ul>
     *
     * <p>
     * Typical users include {@code action} and {@code notification} (in its YANG 1.1 form).
     *
     * @param <D> Declared Statement representation
     * @return A StatementPolicy
     */
    @SuppressWarnings("unchecked")
    public static final <D extends DeclaredStatement<QName>> StatementPolicy<QName, D> uninstantiatedPolicy() {
        return (StatementPolicy<QName, D>) UNINSTANTIATED_POLICY;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This method ensures the statement is added to its parent {@link SchemaTreeNamespace}.
     */
    @Override
    public void onStatementAdded(final Mutable<QName, D, E> stmt) {
        stmt.coerceParentContext().addToNs(SchemaTreeNamespace.class, stmt.getArgument(), stmt);
    }

    // Non-final because {@code input} and {@code output} are doing their own thing.
    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }
}
