/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OperationDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * Common superclass for {@link ActionStatementSupport} and {@link RpcStatementSupport}.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
abstract sealed class AbstractOperationStatementSupport<D extends OperationDeclaredStatement,
        E extends SchemaTreeEffectiveStatement<D>>
        extends AbstractSchemaTreeStatementSupport<D, E>
        permits ActionStatementSupport, RpcStatementSupport {
    AbstractOperationStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<QName, D> policy, final YangParserConfiguration config,
            final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
    }

    @Override
    public final void onFullDefinitionDeclared(final Mutable<QName, D, E> stmt) {
        super.onFullDefinitionDeclared(stmt);

        boolean needInput = true;
        boolean needOutput = true;
        for (var sub : stmt.declaredSubstatements()) {
            if (sub.producesDeclared(InputStatement.class)) {
                needInput = false;
            }
            if (sub.producesDeclared(OutputStatement.class)) {
                needOutput = false;
            }
        }

        if (needInput) {
            appendImplicitSubstatement(stmt, YangStmtMapping.INPUT);
        }
        if (needOutput) {
            appendImplicitSubstatement(stmt, YangStmtMapping.OUTPUT);
        }
    }

    private static void appendImplicitSubstatement(final @NonNull Mutable<?, ?, ?> stmt,
            final @NonNull StatementDefinition def) {
        final var statementName = def.getStatementName();
        final var support = stmt.namespaceItem(StatementSupport.NAMESPACE, statementName);
        if (support == null) {
            throw new VerifyException("No support for " + statementName + " registered");
        }
        stmt.addEffectiveSubstatement(stmt.createUndeclaredSubstatement(support, null));
    }
}
