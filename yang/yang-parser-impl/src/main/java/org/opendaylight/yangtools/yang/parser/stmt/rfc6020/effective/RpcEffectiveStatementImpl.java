/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class RpcEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<QName, RpcStatement> implements RpcDefinition {
    private final QName qname;
    private final SchemaPath path;

    private ContainerSchemaNode input;
    private ContainerSchemaNode output;

    private final Set<TypeDefinition<?>> typeDefinitions;
    private final Set<GroupingDefinition> groupings;
    private final List<UnknownSchemaNode> unknownNodes;

    public RpcEffectiveStatementImpl(final StmtContext<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        Set<GroupingDefinition> groupingsInit = new HashSet<>();
        Set<TypeDefinition<?>> typeDefinitionsInit = new HashSet<>();

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if (effectiveStatement instanceof GroupingDefinition) {
                GroupingDefinition groupingDefinition = (GroupingDefinition) effectiveStatement;
                groupingsInit.add(groupingDefinition);
            }
            if (effectiveStatement instanceof TypedefEffectiveStatement) {
                TypeDefinition<?> typeDefinition = ((TypedefEffectiveStatement) effectiveStatement).getTypeDefinition();
                typeDefinitionsInit.add(typeDefinition);
            }
            if (this.input == null && effectiveStatement instanceof InputEffectiveStatementImpl) {
                this.input = (InputEffectiveStatementImpl) effectiveStatement;
            }
            if (this.output == null && effectiveStatement instanceof OutputEffectiveStatementImpl) {
                this.output = (OutputEffectiveStatementImpl) effectiveStatement;
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.groupings = ImmutableSet.copyOf(groupingsInit);
        this.typeDefinitions = ImmutableSet.copyOf(typeDefinitionsInit);
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return path;
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
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(path);
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
        return Objects.equals(qname, other.qname) && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(RpcEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("qname=");
        sb.append(qname);
        sb.append(", path=");
        sb.append(path);
        sb.append(", input=");
        sb.append(input);
        sb.append(", output=");
        sb.append(output);
        sb.append("]");
        return sb.toString();
    }
}
