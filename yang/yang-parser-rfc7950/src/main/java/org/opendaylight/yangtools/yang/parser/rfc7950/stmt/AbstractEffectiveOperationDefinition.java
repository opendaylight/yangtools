/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class AbstractEffectiveOperationDefinition<D extends DeclaredStatement<QName>>
        extends AbstractEffectiveSchemaNode<D> implements OperationDefinition {
    private final ImmutableSet<TypeDefinition<?>> typeDefinitions;
    private final ImmutableSet<GroupingDefinition> groupings;
    private final ContainerSchemaNode input;
    private final ContainerSchemaNode output;

    protected AbstractEffectiveOperationDefinition(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
        input = findAsContainer(this, InputEffectiveStatement.class);
        output = findAsContainer(this, OutputEffectiveStatement.class);

        // initSubstatements
        final Set<GroupingDefinition> groupingsInit = new HashSet<>();
        final Set<TypeDefinition<?>> mutableTypeDefinitions = new LinkedHashSet<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof GroupingDefinition) {
                final GroupingDefinition groupingDefinition = (GroupingDefinition) effectiveStatement;
                groupingsInit.add(groupingDefinition);
            }
            if (effectiveStatement instanceof TypedefEffectiveStatement) {
                final TypedefEffectiveStatement typeDef = (TypedefEffectiveStatement) effectiveStatement;
                final TypeDefinition<?> type = typeDef.getTypeDefinition();
                if (!mutableTypeDefinitions.contains(type)) {
                    mutableTypeDefinitions.add(type);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, effectiveStatement);
                }
            }
        }
        this.groupings = ImmutableSet.copyOf(groupingsInit);
        this.typeDefinitions = ImmutableSet.copyOf(mutableTypeDefinitions);
    }

    private static ContainerSchemaNode findAsContainer(final EffectiveStatement<?, ?> parent,
            final Class<? extends EffectiveStatement<QName, ?>> statementType) {
        final EffectiveStatement<?, ?> statement = parent.findFirstEffectiveSubstatement(statementType).get();
        Verify.verify(statement instanceof ContainerSchemaNode, "Child statement %s is not a ContainerSchemaNode");
        return (ContainerSchemaNode) statement;
    }

    @Override
    public final ContainerSchemaNode getInput() {
        return input;
    }

    @Override
    public final ContainerSchemaNode getOutput() {
        return output;
    }

    @Override
    public final Set<TypeDefinition<?>> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public final Set<GroupingDefinition> getGroupings() {
        return groupings;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getQName(), getPath());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractEffectiveOperationDefinition<?> other =
                (AbstractEffectiveOperationDefinition<?>) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).add("input", input)
                .add("output", output).toString();
    }
}
