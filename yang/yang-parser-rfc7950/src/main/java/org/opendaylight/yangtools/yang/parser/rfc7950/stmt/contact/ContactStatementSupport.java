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
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractInternedStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ContactStatementSupport
        extends AbstractInternedStringStatementSupport<ContactStatement, ContactEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.CONTACT).build();
    private static final ContactStatementSupport INSTANCE = new ContactStatementSupport();

    private ContactStatementSupport() {
        super(YangStmtMapping.CONTACT, StatementPolicy.reject());
    }

    public static ContactStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ContactStatement createDeclared(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createContact(argument, substatements);
    }

    @Override
    protected ContactStatement createEmptyDeclared(final String argument) {
        return DeclaredStatements.createContact(argument);
    }

    @Override
    protected ContactEffectiveStatement createEffective(final ContactStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createContact(declared, substatements);
    }

    @Override
    protected ContactEffectiveStatement createEmptyEffective(final ContactStatement declared) {
        return EffectiveStatements.createContact(declared);
    }
}