/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class UnknownEffectiveStatementBase<A, D extends UnknownStatement<A>>
        extends AbstractEffectiveDocumentedNodeWithStatus<A, D> implements UnknownSchemaNode {

    private final boolean addedByUses;
    private final boolean augmenting;

    private final ExtensionDefinition extension;
    private final QName nodeType;
    private final String nodeParameter;

    protected UnknownEffectiveStatementBase(final StmtContext<A, D, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(ctx, substatements);

        final StmtContext<?, ExtensionStatement, ExtensionEffectiveStatement> extensionInit =
                ctx.getFromNamespace(ExtensionNamespace.class, ctx.getPublicDefinition().getStatementName());

        if (extensionInit == null) {
            extension = null;
            nodeType = ctx.getPublicDefinition().getStatementName();
        } else {
            final EffectiveStatement<QName, ExtensionStatement> effective = extensionInit.buildEffective();
            Preconditions.checkState(effective instanceof ExtensionDefinition,
                "Statement %s is not an ExtensionDefinition", effective);
            extension = (ExtensionDefinition) extensionInit.buildEffective();
            nodeType = null;
        }

        // initCopyType
        final CopyHistory copyTypesFromOriginal = ctx.getCopyHistory();
        if (copyTypesFromOriginal.contains(CopyType.ADDED_BY_USES_AUGMENTATION)) {
            this.augmenting = true;
            this.addedByUses = true;
        } else {
            this.augmenting = copyTypesFromOriginal.contains(CopyType.ADDED_BY_AUGMENTATION);
            this.addedByUses = copyTypesFromOriginal.contains(CopyType.ADDED_BY_USES);
        }

        nodeParameter = ctx.rawStatementArgument() == null ? "" : ctx.rawStatementArgument();
    }

    @Deprecated
    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public QName getNodeType() {
        return extension == null ? nodeType : extension.getQName();
    }

    @Override
    public String getNodeParameter() {
        return nodeParameter;
    }

    @Deprecated
    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public ExtensionDefinition getExtensionDefinition() {
        return extension;
    }

    @Override
    public String toString() {
        final QName type = getNodeType();
        return String.valueOf(type.getNamespace()) + ":" + type.getLocalName() + " " + nodeParameter;
    }
}
