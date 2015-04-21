/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class RpcEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<QName, RpcStatement> implements RpcDefinition {
    private final QName qname;
    private final SchemaPath path;

    private ContainerSchemaNode input;
    private ContainerSchemaNode output;

    ImmutableSet<TypeDefinition<?>> typeDefinitions;
    ImmutableSet<GroupingDefinition> groupings;
    ImmutableList<UnknownSchemaNode> unknownNodes;

    public RpcEffectiveStatementImpl(StmtContext<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);

        initSubstatements();

    }

    private void initSubstatements() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodes = new LinkedList<>();
        Set<GroupingDefinition> groupings = new HashSet<>();
        Set<TypeDefinition<?>> typeDefinitions = new HashSet<>();

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodes.add(unknownNode);
            }
            if (effectiveStatement instanceof GroupingDefinition) {
                GroupingDefinition groupingDefinition = (GroupingDefinition) effectiveStatement;
                groupings.add(groupingDefinition);
            }
            if (effectiveStatement instanceof TypeDefinition) {
                TypeDefinition<?> typeDefinition = (TypeDefinition<?>) effectiveStatement;
                typeDefinitions.add(typeDefinition);
            }
            if (this.input == null && effectiveStatement instanceof InputEffectiveStatementImpl) {
                this.input = (InputEffectiveStatementImpl) effectiveStatement;
            }
            if (this.output == null && effectiveStatement instanceof OutputEffectiveStatementImpl) {
                this.output = (OutputEffectiveStatementImpl) effectiveStatement;
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodes);
        this.groupings = ImmutableSet.copyOf(groupings);
        this.typeDefinitions = ImmutableSet.copyOf(typeDefinitions);
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

    void setInput(final ContainerSchemaNode input) {
        this.input = input;
    }

    @Override
    public ContainerSchemaNode getOutput() {
        return output;
    }

    void setOutput(final ContainerSchemaNode output) {
        this.output = output;
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
        result = prime * result + ((qname == null) ? 0 : qname.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        if (qname == null) {
            if (other.qname != null) {
                return false;
            }
        } else if (!qname.equals(other.qname)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
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