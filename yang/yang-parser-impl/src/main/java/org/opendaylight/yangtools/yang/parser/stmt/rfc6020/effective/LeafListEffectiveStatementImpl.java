/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public final class LeafListEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<LeafListStatement> implements
        LeafListSchemaNode, DerivableSchemaNode {

    private static final String ORDER_BY_USER_KEYWORD = "user";
    private final TypeDefinition<?> type;
    private final LeafListSchemaNode original;
    private final boolean userOrdered;
    private final Set<String> defaultValues;
    private final QNameModule defaultValueModule;

    public LeafListEffectiveStatementImpl(
            final StmtContext<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> ctx) {
        super(ctx);
        this.original = (LeafListSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
            firstSubstatementOfType(TypeEffectiveStatement.class), ctx.getStatementSourceReference(),
            "Leaf-list is missing a 'type' statement");

        final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
            ctx.getSchemaPath().get());
        final ImmutableSet.Builder<String> defaultValuesBuilder = ImmutableSet.builder();
        QNameModule dfltMod = null;
        boolean isUserOrdered = false;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof OrderedByEffectiveStatementImpl) {
                isUserOrdered = ORDER_BY_USER_KEYWORD.equals(stmt.argument());
            }

            if (stmt instanceof DefaultEffectiveStatementImpl) {
                defaultValuesBuilder.add(((DefaultEffectiveStatementImpl) stmt).argument());
                dfltMod = ((DefaultEffectiveStatementImpl)stmt).getModule();
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

        defaultValues = defaultValuesBuilder.build();
        defaultValueModule = dfltMod;
        SourceException.throwIf(
                TypeUtils.hasDefaultValueMarkedWithIfFeature(ctx.getRootVersion(), typeStmt, defaultValues),
                ctx.getStatementSourceReference(),
                "Leaf-list '%s' has one of its default values '%s' marked with an if-feature statement.",
                ctx.getStatementArgument(), defaultValues);

        type = builder.build();
        userOrdered = isUserOrdered;
    }

    @Override
    public Collection<String> getDefaults() {
        return defaultValues;
    }

    @Override
    public QNameModule getDefaultValueModule() {
        return defaultValueModule;
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
        final LeafListEffectiveStatementImpl other = (LeafListEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return LeafListEffectiveStatementImpl.class.getSimpleName() + "[" +
                getQName() +
                "]";
    }
}
