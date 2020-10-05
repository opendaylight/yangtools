/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.must;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.XPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

public final class MustStatementSupport
        extends BaseStatementSupport<QualifiedBound, MustStatement, MustEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .MUST)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.ERROR_APP_TAG)
        .addOptional(YangStmtMapping.ERROR_MESSAGE)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();

    private final @NonNull XPathSupport xpathSupport;

    private MustStatementSupport(final XPathSupport xpathSupport) {
        super(YangStmtMapping.MUST);
        this.xpathSupport = requireNonNull(xpathSupport);
    }

    public static MustStatementSupport createInstance(final XPathSupport xpathSupport) {
        return new MustStatementSupport(xpathSupport);
    }

    @Override
    public QualifiedBound parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return xpathSupport.parseXPath(ctx, value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected MustStatement createDeclared(final StmtContext<QualifiedBound, MustStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularMustStatement(ctx, substatements);
    }

    @Override
    protected MustStatement createEmptyDeclared(final StmtContext<QualifiedBound, MustStatement, ?> ctx) {
        return new EmptyMustStatement(ctx);
    }

    @Override
    protected MustEffectiveStatement createEffective(
            final StmtContext<QualifiedBound, MustStatement, MustEffectiveStatement> ctx,
            final MustStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularMustEffectiveStatement(declared, substatements);
    }

    @Override
    protected MustEffectiveStatement createEmptyEffective(
            final StmtContext<QualifiedBound, MustStatement, MustEffectiveStatement> ctx,
            final MustStatement declared) {
        return new EmptyMustEffectiveStatement(declared);
    }
}