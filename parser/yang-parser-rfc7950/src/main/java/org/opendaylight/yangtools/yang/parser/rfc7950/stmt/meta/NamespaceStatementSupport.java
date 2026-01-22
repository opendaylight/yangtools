/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class NamespaceStatementSupport
        extends AbstractStatementSupport<XMLNamespace, NamespaceStatement, NamespaceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(NamespaceStatement.DEFINITION).build();

    public NamespaceStatementSupport(final YangParserConfiguration config) {
        super(NamespaceStatement.DEFINITION, StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public XMLNamespace parseArgumentValue(final StmtContext<?, ?,?> ctx, final String value) {
        try {
            return XMLNamespace.of(value).intern();
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid namespace \"%s\"", value);
        }
    }

    @Override
    protected NamespaceStatement createDeclared(final BoundStmtCtx<XMLNamespace> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createNamespace(ctx.getArgument(), substatements);
    }

    @Override
    protected NamespaceStatement attachDeclarationReference(final NamespaceStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateNamespace(stmt, reference);
    }

    @Override
    protected NamespaceEffectiveStatement createEffective(final Current<XMLNamespace, NamespaceStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createNamespace(stmt.declared(), substatements);
    }
}
