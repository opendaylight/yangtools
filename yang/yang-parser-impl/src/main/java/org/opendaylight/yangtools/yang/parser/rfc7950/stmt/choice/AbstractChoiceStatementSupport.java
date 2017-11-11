/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ChildSchemaNodes;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangValidationBundles;

abstract class AbstractChoiceStatementSupport extends
        AbstractQNameStatementSupport<ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> {
    AbstractChoiceStatementSupport() {
        super(YangStmtMapping.CHOICE);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.qnameFromArgument(ctx, value);
    }

    @Override
    public final Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(final StatementDefinition stmtDef) {
        if (YangValidationBundles.SUPPORTED_CASE_SHORTHANDS.contains(stmtDef)) {
            return Optional.of(implictCase());
        }
        return Optional.empty();
    }

    @Override
    public final void onStatementAdded(
            final Mutable<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> stmt) {
        stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
    }

    @Override
    public final ChoiceStatement createDeclared(final StmtContext<QName, ChoiceStatement, ?> ctx) {
        return new ChoiceStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<QName, ChoiceStatement> createEffective(
            final StmtContext<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> ctx) {
        return new ChoiceEffectiveStatementImpl(ctx);
    }

    abstract StatementSupport<?, ?, ?> implictCase();
}