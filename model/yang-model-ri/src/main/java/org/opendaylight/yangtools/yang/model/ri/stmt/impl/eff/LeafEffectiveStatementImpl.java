/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MandatoryMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;

public final class LeafEffectiveStatementImpl
        extends AbstractDeclaredEffectiveStatement.Default<QName, LeafStatement>
        implements LeafEffectiveStatement, LeafSchemaNode, DataSchemaNodeMixin<LeafStatement>,
            MandatoryMixin<QName, LeafStatement>, MustConstraintMixin<QName, LeafStatement> {
    private final @NonNull Object substatements;
    private final @NonNull QName argument;
    private final @NonNull TypeDefinition<?> type;
    private final int flags;

    public LeafEffectiveStatementImpl(final LeafStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.argument = requireNonNull(argument);
        this.substatements = maskList(substatements);
        this.flags = flags;
        // TODO: lazy instantiation?
        type = buildType();
    }

    public LeafEffectiveStatementImpl(final LeafEffectiveStatementImpl original, final QName argument,
            final int flags) {
        super(original);
        this.argument = requireNonNull(argument);
        substatements = original.substatements;
        this.flags = flags;
        // FIXME: share with original?
        type = buildType();
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public LeafEffectiveStatement asEffectiveStatement() {
        return this;
    }

    private TypeDefinition<?> buildType() {
        final TypeEffectiveStatement<?> typeStmt = findFirstEffectiveSubstatement(TypeEffectiveStatement.class).get();
        final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
            getQName());
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DefaultEffectiveStatement) {
                builder.setDefaultValue(((DefaultEffectiveStatement)stmt).argument());
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
        return builder.build();
    }
}
