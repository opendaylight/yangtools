/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class UsesEffectiveStatementImpl extends EffectiveStatementBase<QName, UsesStatement> implements UsesNode {
    private SchemaPath groupingPath;
    private boolean addedByUses;
    ImmutableMap<SchemaPath, SchemaNode> refines;
    ImmutableSet<AugmentationSchema> augmentations;
    ImmutableList<UnknownSchemaNode> unknownNodes;

    public UsesEffectiveStatementImpl(StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> ctx) {
        super(ctx);

        initGroupingPath(ctx);
        initCopyType(ctx);
        initSubstatementCollections();
    }

    private void initGroupingPath(StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> ctx) {
        StmtContext<?, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> grpCtx = ctx.getFromNamespace(
                GroupingNamespace.class, ctx.getStatementArgument());
        this.groupingPath = Utils.getSchemaPath(grpCtx);
    }

    private void initSubstatementCollections() {
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
                refinesInit.put(Utils.SchemaNodeIdentifierToSchemaPath(identifier), refineStmt.getRefineTargetNode());
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.refines = ImmutableMap.copyOf(refinesInit);
    }

    private void initCopyType(
            StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> ctx) {

        Set<TypeOfCopy> copyTypesFromOriginal = StmtContextUtils.getCopyTypesFromOriginal(ctx);
        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        }
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

    void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
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
        result = prime * result + ((groupingPath == null) ? 0 : groupingPath.hashCode());
        result = prime * result + ((augmentations == null) ? 0 : augmentations.hashCode());
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
        if (groupingPath == null) {
            if (other.groupingPath != null) {
                return false;
            }
        } else if (!groupingPath.equals(other.groupingPath)) {
            return false;
        }
        if (augmentations == null) {
            if (other.augmentations != null) {
                return false;
            }
        } else if (!augmentations.equals(other.augmentations)) {
            return false;
        }
        return true;
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