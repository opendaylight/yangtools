/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AbstractEffectiveSchemaNode;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.InputEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.OutputEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.TypeDefEffectiveStatementImpl;

public class ActionEffectiveStatementImpl extends AbstractEffectiveSchemaNode<ActionStatement>
        implements ActionDefinition {
    private final ContainerSchemaNode input;
    private final ContainerSchemaNode output;
    private final Set<TypeDefinition<?>> typeDefinitions;
    private final Set<GroupingDefinition> groupings;

    public ActionEffectiveStatementImpl(
            final StmtContext<QName, ActionStatement, EffectiveStatement<QName, ActionStatement>> ctx) {
        super(ctx);
        this.input = firstEffective(InputEffectiveStatementImpl.class);
        this.output = firstEffective(OutputEffectiveStatementImpl.class);

        // initSubstatements
        Set<GroupingDefinition> groupingsInit = new HashSet<>();
        Set<TypeDefinition<?>> mutableTypeDefinitions = new LinkedHashSet<>();
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof GroupingDefinition) {
                GroupingDefinition groupingDefinition = (GroupingDefinition) effectiveStatement;
                groupingsInit.add(groupingDefinition);
            }
            if (effectiveStatement instanceof TypeDefEffectiveStatementImpl) {
                TypeDefEffectiveStatementImpl typeDef = (TypeDefEffectiveStatementImpl) effectiveStatement;
                TypeDefinition<?> type = typeDef.getTypeDefinition();
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

    @Override
    public ContainerSchemaNode getInput() {
        return input;
    }

    @Override
    public ContainerSchemaNode getOutput() {
        return output;
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return groupings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQName(), getPath());
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

        final ActionEffectiveStatementImpl other = (ActionEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).add("input", input)
                .add("output", output).toString();
    }
}
