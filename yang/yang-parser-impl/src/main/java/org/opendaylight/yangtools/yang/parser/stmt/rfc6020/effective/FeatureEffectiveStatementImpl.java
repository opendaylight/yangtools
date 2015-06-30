/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

import com.google.common.collect.ImmutableList;

public class FeatureEffectiveStatementImpl extends EffectiveStatementBase<QName, FeatureStatement> implements
        FeatureDefinition {

    private QName qName;
    private SchemaPath path;
    private List<UnknownSchemaNode> unknownSchemaNodes;
    private String description;
    private String reference;
    private Status status;

    public FeatureEffectiveStatementImpl(StmtContext<QName, FeatureStatement, ?> ctx) {
        super(ctx);

        this.qName = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);

        initFields();
    }

    private void initFields() {

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                description = ((DescriptionEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof StatusEffectiveStatementImpl) {
                status = ((StatusEffectiveStatementImpl) effectiveStatement).argument();
            }

            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownNodesInit.add((UnknownSchemaNode) effectiveStatement);
            }
        }

        this.unknownSchemaNodes = ImmutableList.copyOf(unknownNodesInit);
    }

    @Override
    public QName getQName() {
        return qName;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qName == null) ? 0 : qName.hashCode());
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
        FeatureEffectiveStatementImpl other = (FeatureEffectiveStatementImpl) obj;
        if (qName == null) {
            if (other.qName != null) {
                return false;
            }
        } else if (!qName.equals(other.qName)) {
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
        StringBuilder sb = new StringBuilder(FeatureEffectiveStatementImpl.class.getSimpleName());
        sb.append("[name=").append(qName).append("]");
        return sb.toString();
    }
}
