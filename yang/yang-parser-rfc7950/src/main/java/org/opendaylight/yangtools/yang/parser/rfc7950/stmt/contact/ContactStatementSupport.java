/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.contact;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ContactStatementSupport
        extends BaseStringStatementSupport<ContactStatement, ContactEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.CONTACT).build();
    private static final ContactStatementSupport INSTANCE = new ContactStatementSupport();

    private ContactStatementSupport() {
        super(YangStmtMapping.CONTACT);
    }

    public static ContactStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ContactStatement createDeclared(final StmtContext<String, ContactStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularContactStatement(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected ContactStatement createEmptyDeclared(final StmtContext<String, ContactStatement, ?> ctx) {
        return new EmptyContactStatement(ctx.coerceRawStatementArgument());
    }

    @Override
    protected ContactEffectiveStatement createEffective(
            final StmtContext<String, ContactStatement, ContactEffectiveStatement> ctx,
            final ContactStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularContactEffectiveStatement(declared, substatements);
    }

    @Override
    protected ContactEffectiveStatement createEmptyEffective(
            final StmtContext<String, ContactStatement, ContactEffectiveStatement> ctx,
            final ContactStatement declared) {
        return new EmptyContactEffectiveStatement(declared);
    }
}