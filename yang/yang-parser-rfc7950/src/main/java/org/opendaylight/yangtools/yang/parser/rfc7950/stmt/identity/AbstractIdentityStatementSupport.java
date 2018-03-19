/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractIdentityStatementSupport
        extends AbstractQNameStatementSupport<IdentityStatement, EffectiveStatement<QName, IdentityStatement>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractIdentityStatementSupport.class);

    AbstractIdentityStatementSupport() {
        super(YangStmtMapping.IDENTITY);
    }

    @SuppressWarnings({"checkstyle:IllegalCatch", "checkstyle:AvoidHidingCauseException"})
    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return StmtContextUtils.parseIdentifier(ctx, value);
        } catch (SourceException e) {
            // FIXME: YANGTOOLS-867: remove this workaround
            final QName ret;
            try {
                ret = StmtContextUtils.qnameFromArgument(ctx, value);
            } catch (RuntimeException re) {
                // Lenient parsing failed, report the original exception
                LOG.debug("Lenient identity parsing failed", re);
                throw e;
            }

            LOG.warn("Worked around illegal identity argument '{}' using lenient parsing", value, e);
            return ret;
        }
    }

    @Override
    public final IdentityStatement createDeclared(final StmtContext<QName, IdentityStatement, ?> ctx) {
        return new IdentityStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<QName, IdentityStatement> createEffective(
            final StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> ctx) {
        return new IdentityEffectiveStatementImpl(ctx);
    }

    @Override
    public final void onStatementDefinitionDeclared(final StmtContext.Mutable<QName, IdentityStatement,
            EffectiveStatement<QName, IdentityStatement>> stmt) {
        stmt.addToNs(IdentityNamespace.class, stmt.getStatementArgument(), stmt);
    }
}