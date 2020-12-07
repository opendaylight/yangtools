/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

/**
 * Specialization of {@link BaseQNameStatementSupport} for {@link SchemaTreeEffectiveStatement} implementations. Every
 * statement automatically participates in {@link SchemaTreeNamespace}.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class BaseSchemaTreeStatementSupport<D extends DeclaredStatement<QName>,
        E extends SchemaTreeEffectiveStatement<D>> extends BaseQNameStatementSupport<D, E> {
    private static final @NonNull StatementPolicy<QName, ?> INSTANTIATED_POLICY =
        StatementPolicy.copyDeclared((copy, stmt, substatements) ->
            copy.effectiveConfig() == copy.effectiveConfig()
// FIXME: actually this
//            if (((AbstractLeafListEffectiveStatement) original).isAddedByUses()
//                    != stmt.history().contains(CopyType.ADDED_BY_USES)) {
//                return false;
//            }
//            if (((AbstractLeafListEffectiveStatement) original).isAugmenting()
//                    != stmt.history().contains(CopyType.ADDED_BY_AUGMENTATION)) {
//                return false;
//            }
            && copy.history().equals(stmt.history())
            // FIXME: should devolve to stmt.getArgument() check
            && copy.schemaPath().equals(stmt.schemaPath()));
    private static final @NonNull StatementPolicy<QName, ?> UNINSTANTIATED_POLICY =
        StatementPolicy.copyDeclared((copy, stmt, substatements) ->
// FIXME: actually this
//            if (((AbstractLeafListEffectiveStatement) original).isAddedByUses()
//                    != stmt.history().contains(CopyType.ADDED_BY_USES)) {
//                return false;
//            }
//            if (((AbstractLeafListEffectiveStatement) original).isAugmenting()
//                    != stmt.history().contains(CopyType.ADDED_BY_AUGMENTATION)) {
//                return false;
//            }
            copy.history().equals(stmt.history())
            // FIXME: should devolve to stmt.getArgument() check
            && copy.schemaPath().equals(stmt.schemaPath()));

    protected BaseSchemaTreeStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<QName, D> policy) {
        super(publicDefinition, policy);
    }

    @Beta
    @SuppressWarnings("unchecked")
    protected static final <D extends DeclaredStatement<QName>>
            @NonNull StatementPolicy<QName, D> instantiatedSchemaTree() {
        return (StatementPolicy<QName, D>) INSTANTIATED_POLICY;
    }

    @Beta
    @SuppressWarnings("unchecked")
    protected static final <D extends DeclaredStatement<QName>>
            @NonNull StatementPolicy<QName, D> uninstantiatedSchemaTree() {
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
