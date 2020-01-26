/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.reference;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping.AbstractGroupingStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ReferenceStatementSupport
        extends BaseStringStatementSupport<ReferenceStatement, ReferenceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REFERENCE)
        .build();
    private static final ReferenceStatementSupport INSTANCE = new ReferenceStatementSupport();

    private ReferenceStatementSupport() {
        super(YangStmtMapping.REFERENCE);
    }

    public static ReferenceStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public ReferenceStatement createDeclared(final StmtContext<String, ReferenceStatement, ?> ctx) {
        return new ReferenceStatementImpl(ctx);
    }

    @Override
    public Optional<? extends Mutable<?, ?, ?>> copyAsChildOf(final Mutable<?, ?, ?> stmt,
            final Mutable<?, ?, ?> parent, final CopyType type, final QNameModule targetModule) {
        return AbstractGroupingStatementSupport.isChildOfGrouping(stmt) ? Optional.empty()
                : super.copyAsChildOf(stmt, parent, type, targetModule);
    }

    @Override
    protected boolean isContextIndependent() {
        return true;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ReferenceEffectiveStatement createEffective(
            final StmtContext<String, ReferenceStatement, ReferenceEffectiveStatement> ctx,
            final ReferenceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularReferenceEffectiveStatement(declared, substatements);
    }

    @Override
    protected ReferenceEffectiveStatement createEmptyEffective(
            final StmtContext<String, ReferenceStatement, ReferenceEffectiveStatement> ctx,
            final ReferenceStatement declared) {
        return new EmptyReferenceEffectiveStatement(declared);
    }
}
