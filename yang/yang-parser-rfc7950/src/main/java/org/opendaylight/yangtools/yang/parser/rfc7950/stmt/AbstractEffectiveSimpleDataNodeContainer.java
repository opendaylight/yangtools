/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveSimpleDataNodeContainer<D extends DeclaredStatement<QName>> extends
        AbstractEffectiveDocumentedDataNodeContainer<QName, D> implements AugmentationTarget, DataSchemaNode {

    private final ImmutableSet<AugmentationSchemaNode> augmentations;
    private final @NonNull ImmutableList<UnknownSchemaNode> unknownNodes;
    private final RevisionAwareXPath whenCondition;
    private final @NonNull SchemaPath path;
    private final boolean configuration;
    private final boolean addedByUses;

    // FIXME: YANGTOOLS-724: this field should be final
    private boolean augmenting;

    protected AbstractEffectiveSimpleDataNodeContainer(final StmtContext<QName, D, ?> ctx) {
        super(ctx);

        this.path = ctx.getSchemaPath().get();
        this.configuration = ctx.isConfiguration();

        whenCondition = findFirstEffectiveSubstatementArgument(WhenEffectiveStatement.class).orElse(null);

        // initSubstatementCollectionsAndFields

        List<UnknownSchemaNode> unknownNodesInit = new ArrayList<>();
        Set<AugmentationSchemaNode> augmentationsInit = new LinkedHashSet<>();
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownNodesInit.add((UnknownSchemaNode) effectiveStatement);
            }
            if (effectiveStatement instanceof AugmentationSchemaNode) {
                augmentationsInit.add((AugmentationSchemaNode) effectiveStatement);
            }
        }
        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);

        // initCopyType
        final CopyHistory copyTypesFromOriginal = ctx.getCopyHistory();
        if (copyTypesFromOriginal.contains(CopyType.ADDED_BY_USES_AUGMENTATION)) {
            this.augmenting = true;
            this.addedByUses = true;
        } else {
            this.augmenting = copyTypesFromOriginal.contains(CopyType.ADDED_BY_AUGMENTATION);
            this.addedByUses = copyTypesFromOriginal.contains(CopyType.ADDED_BY_USES);
        }
    }

    @Override
    public QName getQName() {
        return path.getLastComponent();
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Deprecated
    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Deprecated
    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return augmentations;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public final Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.ofNullable(whenCondition);
    }

    /**
     * Reset {@link #isAugmenting()} to false.
     *
     * @deprecated This method is a violation of immutable contract and is a side-effect of bad/incomplete lifecycle,
     *             which needs to be fixed. Do not introduce new callers. This deficiency is tracked in YANGTOOLS-724.
     */
    @Deprecated
    public final void resetAugmenting() {
        augmenting = false;
    }
}
