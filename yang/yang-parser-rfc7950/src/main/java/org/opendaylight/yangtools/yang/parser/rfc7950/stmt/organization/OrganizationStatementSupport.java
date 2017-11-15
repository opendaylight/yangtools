/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.organization;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OrganizationStatementSupport extends
        AbstractStatementSupport<String, OrganizationStatement, EffectiveStatement<String, OrganizationStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ORGANIZATION)
        .build();
    private static final OrganizationStatementSupport INSTANCE = new OrganizationStatementSupport();

    private OrganizationStatementSupport() {
        super(YangStmtMapping.ORGANIZATION);
    }

    public static OrganizationStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public OrganizationStatement createDeclared(
            final StmtContext<String, OrganizationStatement, ?> ctx) {
        return new OrganizationStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, OrganizationStatement> createEffective(
            final StmtContext<String, OrganizationStatement,
            EffectiveStatement<String, OrganizationStatement>> ctx) {
        return new OrganizationEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}