/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.namespace;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class NamespaceStatementSupport
        extends BaseStatementSupport<URI, NamespaceStatement, NamespaceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .NAMESPACE)
        .build();
    private static final NamespaceStatementSupport INSTANCE = new NamespaceStatementSupport();

    private NamespaceStatementSupport() {
        super(YangStmtMapping.NAMESPACE);
    }

    public static NamespaceStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public URI parseArgumentValue(final StmtContext<?, ?,?> ctx, final String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid namespace \"%s\"", value);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected NamespaceStatement createDeclared(@NonNull final StmtContext<URI, NamespaceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularNamespaceStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected NamespaceStatement createEmptyDeclared(final StmtContext<URI, NamespaceStatement, ?> ctx) {
        return new EmptyNamespaceStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected NamespaceEffectiveStatement createEffective(
            final StmtContext<URI, NamespaceStatement, NamespaceEffectiveStatement> ctx,
            final NamespaceStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularNamespaceEffectiveStatement(declared, substatements);
    }

    @Override
    protected NamespaceEffectiveStatement createEmptyEffective(
            final StmtContext<URI, NamespaceStatement, NamespaceEffectiveStatement> ctx,
            final NamespaceStatement declared) {
        return new EmptyNamespaceEffectiveStatement(declared);
    }
}