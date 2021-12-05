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

public abstract class AbstractLeafEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.Default<QName, LeafStatement>
        implements LeafEffectiveStatement, LeafSchemaNode, DataSchemaNodeMixin<LeafStatement>,
            MandatoryMixin<QName, LeafStatement>, MustConstraintMixin<QName, LeafStatement> {
    private final @NonNull Object substatements;
    // FIXME: YANGTOOLS-1316: this seems to imply that argument.equals(declared.argument()) and we could save a field,
    //                        except we need it in the constructors to materialize type. But if we turn it into a lazy
    //                        field, we should be okay.
    private final @NonNull QName argument;
    private final @NonNull TypeDefinition<?> type;

    private final int flags;

    AbstractLeafEffectiveStatement(final LeafStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.argument = requireNonNull(argument);
        this.substatements = maskList(substatements);
        this.flags = flags;
        type = buildType();
    }

    AbstractLeafEffectiveStatement(final AbstractLeafEffectiveStatement original, final QName argument,
            final int flags) {
        super(original);
        this.argument = requireNonNull(argument);
        substatements = original.substatements;
        this.flags = flags;
        type = buildType();
    }

    @Override
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final QName argument() {
        return argument;
    }

    @Override
    public final TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public final LeafEffectiveStatement asEffectiveStatement() {
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
