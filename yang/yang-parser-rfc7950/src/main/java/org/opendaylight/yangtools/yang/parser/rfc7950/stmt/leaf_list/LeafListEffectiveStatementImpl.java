/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
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
public final class LeafListEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<LeafListStatement>
        implements LeafListEffectiveStatement, LeafListSchemaNode, DerivableSchemaNode {

    private static final String ORDER_BY_USER_KEYWORD = "user";

    private final TypeDefinition<?> type;
    private final LeafListSchemaNode original;
    private final boolean userOrdered;
    private final @NonNull Set<String> defaultValues;
    private final Collection<MustDefinition> mustConstraints;
    private final ElementCountConstraint elementCountConstraint;

    LeafListEffectiveStatementImpl(
            final StmtContext<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> ctx) {
        super(ctx);
        this.original = (LeafListSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
            firstSubstatementOfType(TypeEffectiveStatement.class), ctx.getStatementSourceReference(),
            "Leaf-list is missing a 'type' statement");

        final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
            ctx.getSchemaPath().get());
        final ImmutableSet.Builder<String> defaultValuesBuilder = ImmutableSet.builder();
        boolean isUserOrdered = false;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof OrderedByEffectiveStatement) {
                isUserOrdered = ORDER_BY_USER_KEYWORD.equals(stmt.argument());
            }

            if (stmt instanceof DefaultEffectiveStatement) {
                defaultValuesBuilder.add(((DefaultEffectiveStatement) stmt).argument());
            } else if (stmt instanceof DescriptionEffectiveStatement) {
                builder.setDescription(((DescriptionEffectiveStatement)stmt).argument());
            } else if (stmt instanceof ReferenceEffectiveStatement) {
                builder.setReference(((ReferenceEffectiveStatement)stmt).argument());
            } else if (stmt instanceof StatusEffectiveStatement) {
                builder.setStatus(((StatusEffectiveStatement)stmt).argument());
            } else if (stmt instanceof UnitsEffectiveStatement) {
                builder.setUnits(((UnitsEffectiveStatement)stmt).argument());
            }
        }

        // FIXME: We need to interpret the default value in terms of supplied element type
        defaultValues = defaultValuesBuilder.build();
        SourceException.throwIf(
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(ctx.getRootVersion(), typeStmt, defaultValues),
            ctx.getStatementSourceReference(),
            "Leaf-list '%s' has one of its default values '%s' marked with an if-feature statement.",
            ctx.getStatementArgument(), defaultValues);

        // FIXME: RFC7950 section 7.7.4: we need to check for min-elements and defaultValues conflict

        type = builder.build();
        userOrdered = isUserOrdered;
        elementCountConstraint = EffectiveStmtUtils.createElementCountConstraint(this).orElse(null);
        mustConstraints = ImmutableSet.copyOf(allSubstatementsOfType(MustDefinition.class));
    }

    @Override
    public Collection<String> getDefaults() {
        return defaultValues;
    }

    @Override
    public Optional<LeafListSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
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
    public Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.ofNullable(elementCountConstraint);
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LeafListEffectiveStatementImpl other = (LeafListEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return LeafListEffectiveStatementImpl.class.getSimpleName() + "[" + getQName() + "]";
    }
}
