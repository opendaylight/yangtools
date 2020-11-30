/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnrecognizedEffectiveStatementImpl extends UnknownEffectiveStatementBase<String, UnrecognizedStatement>
        implements UnrecognizedEffectiveStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UnrecognizedEffectiveStatementImpl.class);

    private final QName maybeQNameArgument;
    private final @NonNull SchemaPath path;

    UnrecognizedEffectiveStatementImpl(final Current<String, UnrecognizedStatement> stmt,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);

        // FIXME: Remove following section after fixing 4380
        final UnknownSchemaNode original = (UnknownSchemaNode) stmt.original();
        if (original == null) {
            final QName qname = qnameFromArgument(stmt);
            maybeQNameArgument = qname != null ? qname : getNodeType();
        } else {
            maybeQNameArgument = original.getQName();
        }

        SchemaPath maybePath;
        try {
            maybePath = stmt.getEffectiveParent().schemaPath()
                    .map(parentPath -> parentPath.createChild(maybeQNameArgument)).orElse(null);
        } catch (IllegalArgumentException | SourceException e) {
            LOG.debug("Cannot construct path for {}, attempting to recover", stmt, e);
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

    @Override
    public StatementDefinition statementDefinition() {
        return getDeclared().statementDefinition();
    }

    private static QName qnameFromArgument(final Current<String, UnrecognizedStatement> stmt) {
        final String value = stmt.argument();
        if (value == null || value.isEmpty()) {
            return stmt.publicDefinition().getStatementName();
        }

        final int colon = value.indexOf(':');
        if (colon == -1) {
            if (AbstractQName.isValidLocalName(value)) {
                return QName.unsafeOf(StmtContextUtils.getRootModuleQName(stmt.caerbannog()), value).intern();
            }
            return null;
        }

        final QNameModule qnameModule = StmtContextUtils.getModuleQNameByPrefix(stmt.caerbannog(),
            value.substring(0, colon));
        if (qnameModule == null) {
            return null;
        }

        final int next = value.indexOf(':', colon + 1);
        final String localName = next == -1 ? value.substring(colon + 1) : value.substring(colon + 1, next);
        return QName.create(qnameModule, localName).intern();
    }
}
