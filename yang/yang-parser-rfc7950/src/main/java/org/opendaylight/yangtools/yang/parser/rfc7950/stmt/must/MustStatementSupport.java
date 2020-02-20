/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.must;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.XPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class MustStatementSupport
        extends AbstractStatementSupport<RevisionAwareXPath, MustStatement, MustEffectiveStatement> {
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
    public RevisionAwareXPath parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return xpathSupport.parseXPath(ctx, value);
    }

    @Override
    public MustStatement createDeclared(final StmtContext<RevisionAwareXPath, MustStatement, ?> ctx) {
        return new MustStatementImpl(ctx);
    }

    @Override
    public MustEffectiveStatement createEffective(
            final StmtContext<RevisionAwareXPath, MustStatement, MustEffectiveStatement> ctx) {
        return new MustEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}