/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.UserOrderedMixin;

abstract class AbstractLeafListEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.Default<QName, LeafListStatement>
        implements LeafListEffectiveStatement, LeafListSchemaNode, DerivableSchemaNode,
            UserOrderedMixin<QName, LeafListStatement>, DataSchemaNodeMixin<QName, LeafListStatement>,
            MustConstraintMixin<QName, LeafListStatement> {
    private final @NonNull Object substatements;
    private final @NonNull SchemaPath path;
    private final @NonNull TypeDefinition<?> type;
    private final int flags;

    AbstractLeafListEffectiveStatement(final LeafListStatement declared, final SchemaPath path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.substatements = maskList(substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
        // TODO: lazy instantiation?
        this.type = buildType();
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
    public final @NonNull QName argument() {
        return getQName();
    }

    @Override
    @Deprecated
    public final SchemaPath getPath() {
        return path;
    }

    @Override
    public final TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public final boolean isUserOrdered() {
        return userOrdered();
    }

    @Override
    public final LeafListEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + getQName() + "]";
    }

    private TypeDefinition<?> buildType() {
        final TypeEffectiveStatement<?> typeStmt = findFirstEffectiveSubstatement(TypeEffectiveStatement.class).get();
        final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
            getPath());
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            // NOTE: 'default' is ommitted here on purpose
            if (stmt instanceof DescriptionEffectiveStatement) {
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
