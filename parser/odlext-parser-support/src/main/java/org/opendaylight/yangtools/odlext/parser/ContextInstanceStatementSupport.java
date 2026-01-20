/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.odlext.model.api.ContextInstanceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.ContextInstanceStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class ContextInstanceStatementSupport
        extends AbstractIdentityAwareStatementSupport<ContextInstanceStatement, ContextInstanceEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(ContextInstanceStatement.DEFINITION).build();

    public ContextInstanceStatementSupport(final YangParserConfiguration config) {
        super(ContextInstanceStatement.DEFINITION, config, VALIDATOR);
    }

    @Override
    protected ContextInstanceStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new ContextInstanceStatementImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected ContextInstanceStatement attachDeclarationReference(final ContextInstanceStatement stmt,
            final DeclarationReference reference) {
        return new RefContextInstanceStatement(stmt, reference);
    }

    @Override
    ContextInstanceEffectiveStatement createEffective(final ContextInstanceStatement declared,
            final IdentityEffectiveStatement identity,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ContextInstanceEffectiveStatementImpl(declared, identity, substatements);
    }
}
