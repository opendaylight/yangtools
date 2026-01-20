/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ContextReferenceStatementSupport
        extends AbstractIdentityAwareStatementSupport<ContextReferenceStatement, ContextReferenceEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(ContextReferenceStatement.DEFINITION).build();

    public ContextReferenceStatementSupport(final YangParserConfiguration config) {
        super(ContextReferenceStatement.DEFINITION, config, VALIDATOR);
    }

    @Override
    protected ContextReferenceStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new ContextReferenceStatementImpl(ctx.rawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected ContextReferenceStatement attachDeclarationReference(final ContextReferenceStatement stmt,
            final DeclarationReference reference) {
        return new RefContextReferenceStatement(stmt, reference);
    }

    @Override
    ContextReferenceEffectiveStatement createEffective(final ContextReferenceStatement declared,
            final IdentityEffectiveStatement identity,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ContextReferenceEffectiveStatementImpl(declared, identity, substatements);
    }
}
