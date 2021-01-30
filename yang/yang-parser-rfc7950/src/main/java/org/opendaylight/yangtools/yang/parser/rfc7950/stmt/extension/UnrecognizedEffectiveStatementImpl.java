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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnrecognizedEffectiveStatementImpl extends UnknownEffectiveStatementBase<String, UnrecognizedStatement>
        implements UnrecognizedEffectiveStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UnrecognizedEffectiveStatementImpl.class);

    private final QName maybeQNameArgument;
    private final @Nullable SchemaPath path;

    UnrecognizedEffectiveStatementImpl(final Current<String, UnrecognizedStatement> stmt,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName qnameArgument) {
        super(stmt, substatements);
        this.maybeQNameArgument = qnameArgument != null ? qnameArgument : getNodeType();

        SchemaPath maybePath;
        try {
            maybePath = stmt.getEffectiveParent().schemaPath()
                    .map(parentPath -> parentPath.createChild(maybeQNameArgument)).orElse(null);
        } catch (IllegalArgumentException | SourceException e) {
            LOG.debug("Cannot construct path for {}, attempting to recover", stmt, e);
            maybePath = null;
        }
        path = SchemaPathSupport.toOptionalPath(maybePath);
    }

    @Override
    public QName getQName() {
        return maybeQNameArgument;
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return SchemaNodeDefaults.throwUnsupportedIfNull(this, path);
    }

    @Override
    public StatementDefinition statementDefinition() {
        return getDeclared().statementDefinition();
    }
}
