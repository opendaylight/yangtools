/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.when;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.XPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

public final class WhenStatementSupport
        extends BaseStatementSupport<QualifiedBound, WhenStatement, WhenEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.WHEN)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();

    private final @NonNull XPathSupport xpathSupport;

    private WhenStatementSupport(final XPathSupport xpathSupport) {
        super(YangStmtMapping.WHEN);
        this.xpathSupport = requireNonNull(xpathSupport);
    }

    public static WhenStatementSupport createInstance(final XPathSupport xpathSupport) {
        return new WhenStatementSupport(xpathSupport);
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
    protected WhenStatement createDeclared(final StmtContext<QualifiedBound, WhenStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularWhenStatement(ctx, substatements);
    }

    @Override
    protected WhenStatement createEmptyDeclared(final StmtContext<QualifiedBound, WhenStatement, ?> ctx) {
        return new EmptyWhenStatement(ctx);
    }

    @Override
    protected WhenEffectiveStatement createEffective(
            final StmtContext<QualifiedBound, WhenStatement, WhenEffectiveStatement> ctx,
            final WhenStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularWhenEffectiveStatement(declared, substatements);
    }

    @Override
    protected WhenEffectiveStatement createEmptyEffective(
            final StmtContext<QualifiedBound, WhenStatement, WhenEffectiveStatement> ctx,
            final WhenStatement declared) {
        return new EmptyWhenEffectiveStatement(declared);
    }
}
