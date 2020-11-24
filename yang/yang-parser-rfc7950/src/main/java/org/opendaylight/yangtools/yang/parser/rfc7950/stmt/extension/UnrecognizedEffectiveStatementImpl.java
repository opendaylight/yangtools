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
import org.opendaylight.yangtools.yang.common.QName;
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
        if (original != null) {
            this.maybeQNameArgument = original.getQName();
        } else {
            QName maybeQNameArgumentInit = null;
            try {
                maybeQNameArgumentInit = StmtContextUtils.qnameFromArgument(stmt.caerbannog(), argument());
            } catch (SourceException e) {
                LOG.debug("Not constructing QName from {}", argument(), e);
                maybeQNameArgumentInit = getNodeType();
            }
            this.maybeQNameArgument = maybeQNameArgumentInit;
        }

        SchemaPath maybePath;
        try {
            maybePath = stmt.getParent().schemaPath()
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
}
