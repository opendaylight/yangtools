/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.openconfig.stmt;

import java.util.Objects;
import javax.annotation.Nonnull;
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

    private final SchemaPath path;

    OpenConfigVersionEffectiveStatementImpl(final StmtContext<SemVer, OpenConfigVersionStatement, ?> ctx) {
        super(ctx);
        path = ctx.getParentContext().getSchemaPath().get().createChild(getNodeType());
    }

    @Nonnull
    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Nonnull
    @Override
    public SchemaPath getPath() {
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
        OpenConfigVersionEffectiveStatementImpl other = (OpenConfigVersionEffectiveStatementImpl) obj;
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        if (!Objects.equals(getNodeType(), other.getNodeType())) {
            return false;
        }
        if (!Objects.equals(getNodeParameter(), other.getNodeParameter())) {
            return false;
        }
        return true;
    }
}
