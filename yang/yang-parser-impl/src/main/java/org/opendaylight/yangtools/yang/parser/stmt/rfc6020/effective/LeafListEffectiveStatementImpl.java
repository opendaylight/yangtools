/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Collection;
import java.util.LinkedList;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class LeafListEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<QName, LeafListStatement> implements LeafListSchemaNode, DerivableSchemaNode {
    private final QName qname;
    private final SchemaPath path;

    boolean augmenting;
    boolean addedByUses;
    LeafListSchemaNode original;
    boolean configuration;
    ConstraintDefinition constraintsDef;
    TypeDefinition<?> type;
    boolean userOrdered;

    private ImmutableList<UnknownSchemaNode> unknownNodes;

    public LeafListEffectiveStatementImpl(StmtContext<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);
        //:TODO init other fields

        initSubstatementCollections();
    }

    private void initSubstatementCollections() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        LinkedList<UnknownSchemaNode> unknownNodes = new LinkedList<UnknownSchemaNode>();

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodes.add(unknownNode);
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodes);
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
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public Optional<LeafListSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return constraintsDef;
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
        LeafListEffectiveStatementImpl other = (LeafListEffectiveStatementImpl) obj;
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
        StringBuilder sb = new StringBuilder(LeafListEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append(qname);
        sb.append("]");
        return sb.toString();
    }
}