/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;

public class GroupingEffectiveStatementImpl extends
        AbstractEffectiveDocumentedDataNodeContainer<QName, GroupingStatement> implements GroupingDefinition {
    private final QName qname;
    private final SchemaPath path;
    private final boolean addedByUses;
    private final List<UnknownSchemaNode> unknownNodes;

    public GroupingEffectiveStatementImpl(
            final StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> ctx) {
        super(ctx);

        qname = ctx.getStatementArgument();
        path = ctx.getSchemaPath().get();

        // initCopyType
        List<TypeOfCopy> copyTypesFromOriginal = ctx.getCopyHistory();
        if (copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        } else {
            addedByUses = false;
        }

        final Builder<UnknownSchemaNode> b = ImmutableList.builder();
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                b.add((UnknownSchemaNode) effectiveStatement);
            }
        }

        unknownNodes = b.build();
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
    public boolean isAddedByUses() {
        return addedByUses;
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
        final GroupingEffectiveStatementImpl other = (GroupingEffectiveStatementImpl) obj;
        return Objects.equals(qname, other.qname) && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        return GroupingEffectiveStatementImpl.class.getSimpleName() + "[" +
                "qname=" + qname +
                "]";
    }
}
