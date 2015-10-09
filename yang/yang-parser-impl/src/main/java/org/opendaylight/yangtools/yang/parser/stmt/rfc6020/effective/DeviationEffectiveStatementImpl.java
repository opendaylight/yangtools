/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class DeviationEffectiveStatementImpl extends EffectiveStatementBase<SchemaNodeIdentifier, DeviationStatement>
        implements Deviation, Immutable {
    private final SchemaPath targetPath;
    private final Deviate deviate;
    private final String reference;
    private final List<UnknownSchemaNode> unknownSchemaNodes;

    public DeviationEffectiveStatementImpl(final StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> ctx) {
        super(ctx);
        this.targetPath = ctx.getStatementArgument().asSchemaPath();

        DeviateEffectiveStatementImpl deviateStmt = firstEffective(DeviateEffectiveStatementImpl.class);
        this.deviate = (deviateStmt == null) ? null : deviateStmt.argument();

        ReferenceEffectiveStatementImpl referenceStmt = firstEffective(ReferenceEffectiveStatementImpl.class);
        this.reference = (referenceStmt == null) ? null : referenceStmt.argument();

        List<UnknownSchemaNode> unknownSchemaNodesInit = new LinkedList<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
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
        result = prime * result + Objects.hashCode(targetPath);
        result = prime * result + Objects.hashCode(deviate);
        result = prime * result + Objects.hashCode(reference);
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
        DeviationEffectiveStatementImpl other = (DeviationEffectiveStatementImpl) obj;
        if (!Objects.equals(targetPath, other.targetPath)) {
            return false;
        }
        if (!Objects.equals(deviate, other.deviate)) {
            return false;
        }
        if (!Objects.equals(reference, other.reference)) {
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
