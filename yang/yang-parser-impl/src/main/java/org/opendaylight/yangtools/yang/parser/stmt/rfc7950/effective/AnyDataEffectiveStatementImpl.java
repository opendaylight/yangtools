/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective;

import com.google.common.base.Optional;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AbstractEffectiveDataSchemaNode;

public class AnyDataEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<AnydataStatement> implements
        AnyDataSchemaNode, DerivableSchemaNode {

    private final AnyDataSchemaNode original;

    public AnyDataEffectiveStatementImpl(
            final StmtContext<QName, AnydataStatement, EffectiveStatement<QName, AnydataStatement>> ctx) {
        super(ctx);
        this.original = ctx.getOriginalCtx() == null ? null : (AnyDataSchemaNode) ctx.getOriginalCtx().buildEffective();
    }

    @Override
    public Optional<AnyDataSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(getPath());
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

        final AnyDataEffectiveStatementImpl other = (AnyDataEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return AnyDataEffectiveStatementImpl.class.getSimpleName() + "[" + "qname=" + getQName() + ", path="
                + getPath() + "]";
    }

    @Override
    public ContainerSchemaNode getSchemaOfAnyData() {
        // TODO Auto-generated method stub
        return null;
    }
}
