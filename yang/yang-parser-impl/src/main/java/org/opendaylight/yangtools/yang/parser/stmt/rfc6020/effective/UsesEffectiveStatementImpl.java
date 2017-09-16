/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class UsesEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<QName, UsesStatement>
        implements UsesNode {
    private final SchemaPath groupingPath;
    private final boolean addedByUses;
    private final Map<SchemaPath, SchemaNode> refines;
    private final Set<AugmentationSchema> augmentations;
    private final List<UnknownSchemaNode> unknownNodes;
    private final RevisionAwareXPath whenCondition;

    public UsesEffectiveStatementImpl(
            final StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> ctx) {
        super(ctx);

        // initGroupingPath
        final StmtContext<?, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> grpCtx =
                ctx.getFromNamespace(GroupingNamespace.class, ctx.getStatementArgument());
        this.groupingPath = grpCtx.getSchemaPath().get();

        // initCopyType
        addedByUses = ctx.getCopyHistory().contains(CopyType.ADDED_BY_USES);

        // initSubstatementCollections
        final List<UnknownSchemaNode> unknownNodesInit = new ArrayList<>();
        final Set<AugmentationSchema> augmentationsInit = new LinkedHashSet<>();
        final Map<SchemaPath, SchemaNode> refinesInit = new HashMap<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                final UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if (effectiveStatement instanceof AugmentationSchema) {
                final AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
            if (effectiveStatement instanceof RefineEffectiveStatementImpl) {
                final RefineEffectiveStatementImpl refineStmt = (RefineEffectiveStatementImpl) effectiveStatement;
                final SchemaNodeIdentifier identifier = refineStmt.argument();
                refinesInit.put(identifier.asSchemaPath(), refineStmt.getRefineTargetNode());
            }
        }
        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.refines = ImmutableMap.copyOf(refinesInit);

        final WhenEffectiveStatementImpl whenStmt = firstEffective(WhenEffectiveStatementImpl.class);
        this.whenCondition = whenStmt == null ? null : whenStmt.argument();
    }

    @Nonnull
    @Override
    public SchemaPath getGroupingPath() {
        return groupingPath;
    }

    @Nonnull
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

    @Nonnull
    @Override
    public Map<SchemaPath, SchemaNode> getRefines() {
        return refines;
    }

    @Nonnull
    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Nonnull
    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.fromNullable(whenCondition);
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
        return UsesEffectiveStatementImpl.class.getSimpleName() + "[groupingPath=" + groupingPath + "]";
    }
}
