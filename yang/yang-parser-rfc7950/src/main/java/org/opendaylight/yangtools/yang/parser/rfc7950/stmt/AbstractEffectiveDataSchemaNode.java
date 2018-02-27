/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveDataSchemaNode<D extends DeclaredStatement<QName>> extends
        AbstractEffectiveSchemaNode<D> implements DataSchemaNode {

    private final RevisionAwareXPath whenCondition;
    private final boolean configuration;
    private final boolean addedByUses;
    private final boolean augmenting;

    protected AbstractEffectiveDataSchemaNode(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
        this.configuration = ctx.isConfiguration();
        whenCondition = findFirstEffectiveSubstatementArgument(WhenEffectiveStatement.class).orElse(null);

        // initCopyType
        final CopyHistory originalHistory = ctx.getCopyHistory();
        if (originalHistory.contains(CopyType.ADDED_BY_USES_AUGMENTATION)) {
            this.augmenting = true;
            this.addedByUses = true;
        } else {
            this.augmenting = originalHistory.contains(CopyType.ADDED_BY_AUGMENTATION);
            this.addedByUses = originalHistory.contains(CopyType.ADDED_BY_USES);
        }
    }

    @Deprecated
    @Override
    public final boolean isAugmenting() {
        return augmenting;
    }

    @Deprecated
    @Override
    public final boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public final boolean isConfiguration() {
        return configuration;
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
        // Intentional no-op
    }
}
