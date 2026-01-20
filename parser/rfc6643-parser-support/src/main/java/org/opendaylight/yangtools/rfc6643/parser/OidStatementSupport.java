/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6643.model.api.ObjectIdentifier;
import org.opendaylight.yangtools.rfc6643.model.api.OidEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.OidStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class OidStatementSupport
        extends AbstractStatementSupport<ObjectIdentifier, OidStatement, OidEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
            SubstatementValidator.builder(OidStatement.DEFINITION).build();

    public OidStatementSupport(final YangParserConfiguration config) {
        super(OidStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    public ObjectIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return ObjectIdentifier.forString(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid object identifier '%s'", value);
        }
    }

    @Override
    protected OidStatement createDeclared(final BoundStmtCtx<ObjectIdentifier> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new OidStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected OidStatement attachDeclarationReference(final OidStatement stmt, final DeclarationReference reference) {
        return new RefOidStatement(stmt, reference);
    }

    @Override
    protected OidEffectiveStatement createEffective(final Current<ObjectIdentifier, OidStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new OidEffectiveStatementImpl(stmt, substatements);
    }
}