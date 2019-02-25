/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
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
        if (original != null) {
            this.maybeQNameArgument = original.getQName();
        } else {
            QName maybeQNameArgumentInit = null;
            try {
                maybeQNameArgumentInit = StmtContextUtils.qnameFromArgument(ctx, argument());
            } catch (SourceException e) {
                LOG.debug("Not constructing QName from {}", argument(), e);
                maybeQNameArgumentInit = getNodeType();
            }
            this.maybeQNameArgument = maybeQNameArgumentInit;
        }
        path = ctx.coerceParentContext().getSchemaPath().get().createChild(maybeQNameArgument);
    }

    @Override
    public QName getQName() {
        return maybeQNameArgument;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(maybeQNameArgument);
        result = prime * result + Objects.hashCode(path);
        result = prime * result + Objects.hashCode(getNodeType());
        result = prime * result + Objects.hashCode(getNodeParameter());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UnrecognizedEffectiveStatementImpl)) {
            return false;
        }
        UnrecognizedEffectiveStatementImpl other = (UnrecognizedEffectiveStatementImpl) obj;
        return Objects.equals(maybeQNameArgument, other.maybeQNameArgument) && Objects.equals(path, other.path)
                && Objects.equals(getNodeType(), other.getNodeType())
                && Objects.equals(getNodeParameter(), other.getNodeParameter());
    }
}
