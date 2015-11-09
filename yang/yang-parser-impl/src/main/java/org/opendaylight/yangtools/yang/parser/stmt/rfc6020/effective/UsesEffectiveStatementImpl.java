/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;

public final class UsesEffectiveStatementImpl extends DeclaredEffectiveStatementBase<QName, UsesStatement> implements UsesNode {
    private final SchemaPath groupingPath;
    private final boolean addedByUses;
    private final Map<SchemaPath, SchemaNode> refines;
    private final Set<AugmentationSchema> augmentations;
    private final List<UnknownSchemaNode> unknownNodes;

    public UsesEffectiveStatementImpl(
            final StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> ctx) {
        super(ctx);

        // initGroupingPath
        StmtContext<?, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> grpCtx = ctx.getFromNamespace(
                GroupingNamespace.class, ctx.getStatementArgument());
        this.groupingPath = grpCtx.getSchemaPath().get();

        // initCopyType
        List<TypeOfCopy> copyTypesFromOriginal = ctx.getCopyHistory();
        if (copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        } else {
            addedByUses = false;
        }

        // initSubstatementCollections
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();
        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        Set<AugmentationSchema> augmentationsInit = new HashSet<>();
        Map<SchemaPath, SchemaNode> refinesInit = new HashMap<>();
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if (effectiveStatement instanceof AugmentationSchema) {
                AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
            if (effectiveStatement instanceof RefineEffectiveStatementImpl) {
                RefineEffectiveStatementImpl refineStmt = (RefineEffectiveStatementImpl) effectiveStatement;
                SchemaNodeIdentifier identifier = refineStmt.argument();
                refinesInit.put(identifier.asSchemaPath(), refineStmt.getRefineTargetNode());
            }
        }
        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.refines = ImmutableMap.copyOf(refinesInit);
    }

    @Override
    public SchemaPath getGroupingPath() {
        return groupingPath;
    }

    @Override
    public Set<AugmentationSchema> getAugmentations() {
        return augmentations;
    }

    @Override
    public boolean isAugmenting() {
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public Map<SchemaPath, SchemaNode> getRefines() {
        return refines;
    }

    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(groupingPath);
        result = prime * result + Objects.hashCode(augmentations);
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
        final UsesEffectiveStatementImpl other = (UsesEffectiveStatementImpl) obj;
        return Objects.equals(groupingPath, other.groupingPath) && Objects.equals(augmentations, other.augmentations);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(UsesEffectiveStatementImpl.class.getSimpleName());
        sb.append("[groupingPath=");
        sb.append(groupingPath);
        sb.append("]");
        return sb.toString();
    }
}
