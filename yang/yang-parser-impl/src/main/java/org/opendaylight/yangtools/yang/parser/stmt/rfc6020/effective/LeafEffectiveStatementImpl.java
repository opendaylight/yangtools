/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class LeafEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<LeafStatement> implements
        LeafSchemaNode, DerivableSchemaNode {
    private final LeafSchemaNode original;
    private final TypeDefinition<?> type;
    private final String defaultStr;
    private final String unitsStr;

    public LeafEffectiveStatementImpl(final StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
        super(ctx);
        this.original = ctx.getOriginalCtx() == null ? null : (LeafSchemaNode) ctx.getOriginalCtx().buildEffective();

        DefaultEffectiveStatementImpl defaultStmt = firstEffective(DefaultEffectiveStatementImpl.class);
        this.defaultStr = (defaultStmt == null) ? null : defaultStmt.argument();

        UnitsEffectiveStatementImpl unitsStmt = firstEffective(UnitsEffectiveStatementImpl.class);
        this.unitsStr = (unitsStmt == null) ? null : unitsStmt.argument();

        // FIXME: need to derive the time
        final TypeEffectiveStatement<?> typeStatement = firstSubstatementOfType(TypeEffectiveStatement.class);
        this.type = typeStatement.getTypeDefinition();
    }

    @Override
    public Optional<LeafSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public String getDefault() {
        return defaultStr;
    }

    @Override
    public String getUnits() {
        return unitsStr;
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
        LeafEffectiveStatementImpl other = (LeafEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(LeafEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("qname=").append(getQName());
        sb.append(", path=").append(getPath());
        sb.append("]");
        return sb.toString();
    }
}
