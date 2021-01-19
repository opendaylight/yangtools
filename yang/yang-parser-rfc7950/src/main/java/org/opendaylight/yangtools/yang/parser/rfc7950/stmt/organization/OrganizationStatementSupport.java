/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.organization;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OrganizationStatementSupport
        extends AbstractStringStatementSupport<OrganizationStatement, OrganizationEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ORGANIZATION)
        .build();
    private static final OrganizationStatementSupport INSTANCE = new OrganizationStatementSupport();

    private OrganizationStatementSupport() {
        super(YangStmtMapping.ORGANIZATION, StatementPolicy.reject());
    }

    public static OrganizationStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected OrganizationStatement createDeclared(final StmtContext<String, OrganizationStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularOrganizationStatement(ctx.getRawArgument(), substatements);
    }

    @Override
    protected OrganizationStatement createEmptyDeclared(final StmtContext<String, OrganizationStatement, ?> ctx) {
        return new EmptyOrganizationStatement(ctx.getRawArgument());
    }

    @Override
    protected OrganizationEffectiveStatement createEffective(final Current<String, OrganizationStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyOrganizationEffectiveStatement(stmt.declared())
            : new RegularOrganizationEffectiveStatement(stmt.declared(), substatements);
    }
}