/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.util.type.TypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.MandatoryMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.MustConstraintMixin;

abstract class AbstractLeafEffectiveStatement extends AbstractDeclaredEffectiveStatement.Default<QName, LeafStatement>
        implements LeafEffectiveStatement, LeafSchemaNode, DerivableSchemaNode,
            DataSchemaNodeMixin<QName, LeafStatement>, MandatoryMixin<QName, LeafStatement>,
            MustConstraintMixin<QName, LeafStatement> {
    private final @NonNull Object substatements;
    private final @NonNull Immutable path;
    private final @NonNull TypeDefinition<?> type;
    private final int flags;

    AbstractLeafEffectiveStatement(final LeafStatement declared, final Immutable path, final int flags,
            final TypeDefinition<?> type, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.path = requireNonNull(path);
        this.type = requireNonNull(type);
        this.substatements = maskList(substatements);
        this.flags = flags;
    }

    AbstractLeafEffectiveStatement(final AbstractLeafEffectiveStatement original, final Immutable path,
            final int flags) {
        super(original);
        this.path = requireNonNull(path);
        this.type = TypeBuilder.copyTypeDefinition(original.type, getQName());
        this.substatements = original.substatements;
        this.flags = flags;
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
        return getQName();
    }

    @Override
    public final Immutable pathObject() {
        return path;
    }

    @Override
    public final TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public final LeafEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
