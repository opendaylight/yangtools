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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class LeafListEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<LeafListStatement> implements
        LeafListSchemaNode, DerivableSchemaNode {

    private static final String ORDER_BY_USER_KEYWORD = "user";
    private final TypeDefinition<?> type;
    private final LeafListSchemaNode original;
    private final boolean userOrdered;

    public LeafListEffectiveStatementImpl(
            final StmtContext<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> ctx) {
        super(ctx);
        this.original = ctx.getOriginalCtx() == null ? null : (LeafListSchemaNode) ctx.getOriginalCtx()
                .buildEffective();

        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
            firstSubstatementOfType(TypeEffectiveStatement.class), ctx.getStatementSourceReference(),
            "Leaf-list is missing a 'type' statement");

        final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
            ctx.getSchemaPath().get());
        boolean isUserOrdered = false;
        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof OrderedByEffectiveStatementImpl) {
                isUserOrdered = ORDER_BY_USER_KEYWORD.equals(stmt.argument());
            }

            if (stmt instanceof DefaultEffectiveStatementImpl) {
                builder.setDefaultValue(stmt.argument());
            } else if (stmt instanceof DescriptionEffectiveStatementImpl) {
                builder.setDescription(((DescriptionEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof ReferenceEffectiveStatementImpl) {
                builder.setReference(((ReferenceEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof StatusEffectiveStatementImpl) {
                builder.setStatus(((StatusEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof UnitsEffectiveStatementImpl) {
                builder.setUnits(((UnitsEffectiveStatementImpl)stmt).argument());
            }
        }

        type = builder.build();
        userOrdered = isUserOrdered;
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
        return LeafListEffectiveStatementImpl.class.getSimpleName() + "[" +
                getQName() +
                "]";
    }
}
