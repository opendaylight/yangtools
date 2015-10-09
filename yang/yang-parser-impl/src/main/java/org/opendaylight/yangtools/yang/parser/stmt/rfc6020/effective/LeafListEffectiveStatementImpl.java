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
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public final class LeafListEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<LeafListStatement> implements
        LeafListSchemaNode, DerivableSchemaNode {

    private final LeafListSchemaNode original;
    private final TypeDefinition<?> type;
    private final boolean userOrdered;
    private static final String ORDER_BY_USER_KEYWORD = "user";

    public LeafListEffectiveStatementImpl(
            final StmtContext<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> ctx) {
        super(ctx);
        this.original = ctx.getOriginalCtx() == null ? null : (LeafListSchemaNode) ctx.getOriginalCtx()
                .buildEffective();

        OrderedByEffectiveStatementImpl orderedByStmt = firstEffective(OrderedByEffectiveStatementImpl.class);
        if (orderedByStmt != null && orderedByStmt.argument().equals(ORDER_BY_USER_KEYWORD)) {
            this.userOrdered = true;
        } else {
            this.userOrdered = false;
        }

        EffectiveStatement<?, ?> typeEffectiveSubstatement = firstEffectiveSubstatementOfType(TypeDefinition.class);
        this.type = TypeUtils.getTypeFromEffectiveStatement(typeEffectiveSubstatement);
    }

    @Override
    public Optional<LeafListSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public boolean isUserOrdered() {
        return userOrdered;
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
        LeafListEffectiveStatementImpl other = (LeafListEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(LeafListEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append(getQName());
        sb.append("]");
        return sb.toString();
    }
}
