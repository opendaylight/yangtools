/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractInternedStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OrganizationStatementSupport
        extends AbstractInternedStringStatementSupport<OrganizationStatement, OrganizationEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.ORGANIZATION).build();

    public OrganizationStatementSupport(final boolean retainDeclarationReference) {
        super(YangStmtMapping.ORGANIZATION, StatementPolicy.reject(), retainDeclarationReference);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected OrganizationStatement createDeclared(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createOrganization(argument, substatements);
    }

    @Override
    protected OrganizationStatement createEmptyDeclared(final String argument) {
        return DeclaredStatements.createOrganization(argument);
    }

    @Override
    protected OrganizationEffectiveStatement createEffective(final OrganizationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createOrganization(declared, substatements);
    }

    @Override
    protected OrganizationEffectiveStatement createEmptyEffective(final OrganizationStatement declared) {
        return EffectiveStatements.createOrganization(declared);
    }
}