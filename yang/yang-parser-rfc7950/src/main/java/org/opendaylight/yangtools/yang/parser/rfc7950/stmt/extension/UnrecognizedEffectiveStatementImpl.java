/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnrecognizedEffectiveStatementImpl extends UnknownEffectiveStatementBase<String, UnrecognizedStatement>
        implements UnrecognizedEffectiveStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UnrecognizedEffectiveStatementImpl.class);

    private final QName maybeQNameArgument;
    private final @NonNull SchemaPath path;

    UnrecognizedEffectiveStatementImpl(final StmtContext<String, UnrecognizedStatement, ?> ctx) {
        super(ctx);

        // FIXME: Remove following section after fixing 4380
        final UnknownSchemaNode original = (UnknownSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective)
                .orElse(null);
        if (original == null) {
            final QName qname = qnameFromArgument(ctx);
            maybeQNameArgument = qname != null ? qname : getNodeType();
        } else {
            maybeQNameArgument = original.getQName();
        }

        SchemaPath maybePath;
        try {
            maybePath = ctx.coerceParentContext().getSchemaPath()
                    .map(parentPath -> parentPath.createChild(maybeQNameArgument)).orElse(null);
        } catch (IllegalArgumentException | SourceException e) {
            LOG.debug("Cannot construct path for {}, attempting to recover", ctx, e);
            maybePath = null;
        }
        path = maybePath;
    }

    @Override
    public QName getQName() {
        return maybeQNameArgument;
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }

    private static QName qnameFromArgument(final StmtContext<String, UnrecognizedStatement, ?> stmt) {
        final String value = stmt.getStatementArgument();
        if (value == null || value.isEmpty()) {
            return stmt.getPublicDefinition().getStatementName();
        }

        final int colon = value.indexOf(':');
        if (colon == -1) {
            final UnqualifiedQName qname = UnqualifiedQName.tryCreate(value);
            return qname == null ? null : qname.bindTo(StmtContextUtils.getRootModuleQName(stmt)).intern();
        }

        final QNameModule qnameModule = StmtContextUtils.getModuleQNameByPrefix(stmt, value.substring(0, colon));
        if (qnameModule == null) {
            return null;
        }

        final int next = value.indexOf(':', colon + 1);
        final String localName = next == -1 ? value.substring(colon + 1) : value.substring(colon + 1, next);
        final UnqualifiedQName qname = UnqualifiedQName.tryCreate(localName);
        return qname == null ? null : qname.bindTo(qnameModule).intern();
    }
}
