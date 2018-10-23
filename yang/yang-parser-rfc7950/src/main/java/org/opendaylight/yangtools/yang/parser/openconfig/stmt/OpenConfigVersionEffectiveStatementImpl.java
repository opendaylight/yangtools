/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.openconfig.stmt;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class OpenConfigVersionEffectiveStatementImpl extends
        UnknownEffectiveStatementBase<SemVer, OpenConfigVersionStatement>
        implements OpenConfigVersionEffectiveStatement {

    private final @NonNull SchemaPath path;

    OpenConfigVersionEffectiveStatementImpl(final StmtContext<SemVer, OpenConfigVersionStatement, ?> ctx) {
        super(ctx);
        path = ctx.getParentContext().getSchemaPath().get().createChild(getNodeType());
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, getNodeType(), getNodeParameter());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpenConfigVersionEffectiveStatementImpl)) {
            return false;
        }
        final OpenConfigVersionEffectiveStatementImpl other = (OpenConfigVersionEffectiveStatementImpl) obj;
        return Objects.equals(path, other.path) && Objects.equals(getNodeType(), other.getNodeType())
                && Objects.equals(getNodeParameter(), other.getNodeParameter());
    }
}
