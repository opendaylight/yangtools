/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDataSchemaNode;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

// FIXME: hide this class
public final class LeafEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<LeafStatement>
        implements LeafEffectiveStatement, LeafSchemaNode, DerivableSchemaNode {
    private final ImmutableSet<MustDefinition> mustConstraints;
    private final LeafSchemaNode original;
    private final TypeDefinition<?> type;
    private final String defaultStr;
    private final String unitsStr;
    private final boolean mandatory;

    LeafEffectiveStatementImpl(final StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
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
            if (stmt instanceof DefaultEffectiveStatement) {
                dflt = ((DefaultEffectiveStatement)stmt).argument();
                builder.setDefaultValue(stmt.argument());
            } else if (stmt instanceof DescriptionEffectiveStatement) {
                builder.setDescription(((DescriptionEffectiveStatement)stmt).argument());
            } else if (stmt instanceof ReferenceEffectiveStatement) {
                builder.setReference(((ReferenceEffectiveStatement)stmt).argument());
            } else if (stmt instanceof StatusEffectiveStatement) {
                builder.setStatus(((StatusEffectiveStatement)stmt).argument());
            } else if (stmt instanceof UnitsEffectiveStatement) {
                units = ((UnitsEffectiveStatement)stmt).argument();
                builder.setUnits(units);
            }
        }

        SourceException.throwIf(
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(ctx.getRootVersion(), typeStmt, dflt),
            ctx.getStatementSourceReference(),
            "Leaf '%s' has default value '%s' marked with an if-feature statement.", ctx.getStatementArgument(), dflt);

        defaultStr = dflt;
        unitsStr = units;
        type = builder.build();
        mandatory = findFirstEffectiveSubstatementArgument(MandatoryEffectiveStatement.class).orElse(Boolean.FALSE)
                .booleanValue();
        mustConstraints = ImmutableSet.copyOf(allSubstatementsOfType(MustDefinition.class));
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public Optional<LeafSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public Collection<MustDefinition> getMustConstraints() {
        return mustConstraints;
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
