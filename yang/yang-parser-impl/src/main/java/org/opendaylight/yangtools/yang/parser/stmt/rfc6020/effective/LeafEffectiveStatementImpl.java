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
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public final class LeafEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<LeafStatement> implements
        LeafSchemaNode, DerivableSchemaNode {
    private final LeafSchemaNode original;
    private final TypeDefinition<?> type;
    private final String defaultStr;
    private final String unitsStr;

    public LeafEffectiveStatementImpl(
            final StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
        super(ctx);
        this.original = (LeafSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
                firstSubstatementOfType(TypeEffectiveStatement.class), ctx.getStatementSourceReference(),
                "Leaf is missing a 'type' statement");

        String dflt = null;
        String units = null;
        final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
            ctx.getSchemaPath().get());
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DefaultEffectiveStatementImpl) {
                dflt = ((DefaultEffectiveStatementImpl)stmt).argument();
                builder.setDefaultValue(stmt.argument());
            } else if (stmt instanceof DescriptionEffectiveStatementImpl) {
                builder.setDescription(((DescriptionEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof ReferenceEffectiveStatementImpl) {
                builder.setReference(((ReferenceEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof StatusEffectiveStatementImpl) {
                builder.setStatus(((StatusEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof UnitsEffectiveStatementImpl) {
                units = ((UnitsEffectiveStatementImpl)stmt).argument();
                builder.setUnits(units);
            }
        }

        SourceException.throwIf(TypeUtils.hasDefaultValueMarkedWithIfFeature(ctx.getRootVersion(), typeStmt, dflt),
                ctx.getStatementSourceReference(),
                "Leaf '%s' has default value '%s' marked with an if-feature statement.", ctx.getStatementArgument(),
                dflt);

        defaultStr = dflt;
        unitsStr = units;
        type = builder.build();
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
        if (!(obj instanceof LeafEffectiveStatementImpl)) {
            return false;
        }
        final LeafEffectiveStatementImpl other = (LeafEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return LeafEffectiveStatementImpl.class.getSimpleName() + "["
                + "qname=" + getQName()
                + ", path=" + getPath()
                + "]";
    }
}
