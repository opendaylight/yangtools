/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Specialization of {@link AbstractQNameStatementSupport} for {@link SchemaTreeEffectiveStatement} implementations.
 * Every statement automatically participates in {@link ParserNamespaces#schemaTree()}.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class AbstractSchemaTreeStatementSupport<D extends DeclaredStatement<QName>,
        E extends SchemaTreeEffectiveStatement<D>> extends AbstractQNameStatementSupport<D, E> {
    private static class SchemaTreeEquality<D extends DeclaredStatement<QName>>
            implements StatementEquality<QName, D> {
        private static final class Instantiated<D extends DeclaredStatement<QName>> extends SchemaTreeEquality<D> {
            @Override
            public boolean canReuseCurrent(final Current<QName, D> copy, final Current<QName, D> current,
                    final Collection<? extends EffectiveStatement<?, ?>> substatements) {
                return copy.effectiveConfig() == current.effectiveConfig()
                    && super.canReuseCurrent(copy, current, substatements);
            }
        }

        @Override
        public boolean canReuseCurrent(final Current<QName, D> copy, final Current<QName, D> current,
                final Collection<? extends EffectiveStatement<?, ?>> substatements) {
            return equalHistory(copy.history(), current.history())
                && copy.getArgument().equals(current.getArgument());
        }

        private static boolean equalHistory(final CopyHistory copy, final CopyHistory current) {
            return copy.isAugmenting() == current.isAugmenting() && copy.isAddedByUses() == current.isAddedByUses();
        }
    }

    private static final StatementPolicy<QName, ?> INSTANTIATED_POLICY =
        StatementPolicy.copyDeclared(new SchemaTreeEquality.Instantiated<>());
    private static final StatementPolicy<QName, ?> UNINSTANTIATED_POLICY =
        StatementPolicy.copyDeclared(new SchemaTreeEquality<>());

    protected AbstractSchemaTreeStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<QName, D> policy, final YangParserConfiguration config,
            final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
    }

    /**
     * Return the {@link StatementPolicy} corresponding to a potentially-instantiated YANG statement. Statements are
     * reused as long as:
     * <ul>
     *   <li>{@link Current#argument()} does not change</li>
     *   <li>{@link Current#history()} does not change as far as {@link CopyableNode} is concerned</li>
     *   <li>{@link Current#effectiveConfig()} does not change</li>
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
     * This method ensures the statement is added to its parent {@link ParserNamespaces#schemaTree()}.
     */
    @Override
    public void onStatementAdded(final Mutable<QName, D, E> stmt) {
        stmt.coerceParentContext().addToNs(ParserNamespaces.schemaTree(), stmt.getArgument(), stmt);
    }

    // Non-final because {@code input} and {@code output} are doing their own thing.
    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }
}
