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

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

import com.google.common.collect.ImmutableList;

public class DeviationEffectiveStatementImpl extends EffectiveStatementBase<SchemaNodeIdentifier, DeviationStatement>
        implements Deviation, Immutable {

    private SchemaPath targetPath;
    private Deviate deviate;
    private String reference;
    private ImmutableList<UnknownSchemaNode> unknownSchemaNodes;

    public DeviationEffectiveStatementImpl(StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> ctx) {
        super(ctx);

        List<UnknownSchemaNode> unknownSchemaNodesInit = new LinkedList<>();

        targetPath = SchemaPath.create(ctx.getStatementArgument().getPathFromRoot(), ctx.getStatementArgument()
                .isAbsolute());

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof DeviateEffectiveStatementImpl) {
                deviate = ((DeviateEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownSchemaNodesInit.add((UnknownSchemaNode) effectiveStatement);
            }
        }

        unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodesInit);
    }

    @Override
    public SchemaPath getTargetPath() {
        return targetPath;
    }

    @Override
    public Deviate getDeviate() {
        return deviate;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((targetPath == null) ? 0 : targetPath.hashCode());
        result = prime * result + ((deviate == null) ? 0 : deviate.hashCode());
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviationEffectiveStatementImpl other = (DeviationEffectiveStatementImpl) obj;
        if (targetPath == null) {
            if (other.targetPath != null) {
                return false;
            }
        } else if (!targetPath.equals(other.targetPath)) {
            return false;
        }
        if (deviate == null) {
            if (other.deviate != null) {
                return false;
            }
        } else if (!deviate.equals(other.deviate)) {
            return false;
        }
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(DeviationEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("targetPath=").append(targetPath);
        sb.append(", deviate=").append(deviate);
        sb.append(", reference=").append(reference);
        sb.append("]");
        return sb.toString();
    }
}