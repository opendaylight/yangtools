/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class RpcEffectiveStatementImpl extends AbstractEffectiveSchemaNode<RpcStatement> implements RpcDefinition {
    private final ContainerSchemaNode input;
    private final ContainerSchemaNode output;
    private final Set<TypeDefinition<?>> typeDefinitions;
    private final Set<GroupingDefinition> groupings;

    public RpcEffectiveStatementImpl(final StmtContext<QName, RpcStatement,
            EffectiveStatement<QName, RpcStatement>> ctx) {
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
        final RpcEffectiveStatementImpl other = (RpcEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return RpcEffectiveStatementImpl.class.getSimpleName() + "["
                + "qname=" + getQName()
                + ", path=" +  getPath()
                + ", input=" + input
                + ", output=" + output
                + "]";
    }
}
