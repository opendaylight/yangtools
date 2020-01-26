/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.description;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping.AbstractGroupingStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DescriptionStatementSupport
        extends BaseStringStatementSupport<DescriptionStatement, DescriptionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.DESCRIPTION).build();
    private static final DescriptionStatementSupport INSTANCE = new DescriptionStatementSupport();

    private DescriptionStatementSupport() {
        super(YangStmtMapping.DESCRIPTION);
    }

    public static DescriptionStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public Optional<? extends Mutable<?, ?, ?>> copyAsChildOf(final Mutable<?, ?, ?> stmt,
            final Mutable<?, ?, ?> parent, final CopyType type, final QNameModule targetModule) {
        return AbstractGroupingStatementSupport.isChildOfGrouping(stmt) ? Optional.empty()
                : super.copyAsChildOf(stmt, parent, type, targetModule);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected DescriptionStatement createDeclared(final StmtContext<String, DescriptionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularDescriptionStatement(ctx, substatements);
    }

    @Override
    protected DescriptionStatement createEmptyDeclared(final StmtContext<String, DescriptionStatement, ?> ctx) {
        return new EmptyDescriptionStatement(ctx);
    }

    @Override
    protected DescriptionEffectiveStatement createEffective(
            final StmtContext<String, DescriptionStatement, DescriptionEffectiveStatement> ctx,
            final DescriptionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularDescriptionEffectiveStatement(declared, substatements);
    }

    @Override
    protected DescriptionEffectiveStatement createEmptyEffective(
            final StmtContext<String, DescriptionStatement, DescriptionEffectiveStatement> ctx,
            final DescriptionStatement declared) {
        return new EmptyDescriptionEffectiveStatement(declared);
    }
}
