/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@VisibleForTesting
public final class AnyxmlSchemaLocationEffectiveStatementImpl
        extends UnknownEffectiveStatementBase<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>
        implements AnyxmlSchemaLocationEffectiveStatement {

    private final @NonNull SchemaPath path;

    AnyxmlSchemaLocationEffectiveStatementImpl(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement, ?> ctx) {
        super(ctx);
        path = ctx.coerceParentContext().getSchemaPath().get().createChild(getNodeType());
    }

    @Override
    public @NonNull QName getQName() {
        return getNodeType();
    }

    @Override
    public @NonNull SchemaPath getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AnyxmlSchemaLocationEffectiveStatementImpl other = (AnyxmlSchemaLocationEffectiveStatementImpl) obj;
        return Objects.equals(path, other.path) && Objects.equals(getNodeType(), other.getNodeType())
                && Objects.equals(getNodeParameter(), other.getNodeParameter());
    }
}
