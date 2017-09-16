/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class UnknownEffectiveStatementBase<A> extends AbstractEffectiveDocumentedNode<A, UnknownStatement<A>>
        implements UnknownSchemaNode {

    private final boolean addedByUses;
    private final boolean addedByAugmentation;

    private final ExtensionDefinition extension;
    private final List<UnknownSchemaNode> unknownNodes;
    private final QName nodeType;
    private final String nodeParameter;

    protected UnknownEffectiveStatementBase(final StmtContext<A, UnknownStatement<A>, ?> ctx) {
        super(ctx);

        final StmtContext<?, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> extensionInit =
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
            this.addedByUses = this.addedByAugmentation = true;
        } else {
            this.addedByAugmentation = copyTypesFromOriginal.contains(CopyType.ADDED_BY_AUGMENTATION);
            this.addedByUses = copyTypesFromOriginal.contains(CopyType.ADDED_BY_USES);
        }

        nodeParameter = ctx.rawStatementArgument() == null ? "" : ctx.rawStatementArgument();

        // TODO init other fields (see Bug1412Test)
        final Builder<UnknownSchemaNode> builder = ImmutableList.builder();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                builder.add((UnknownSchemaNode) effectiveStatement);
            }
        }
        unknownNodes = builder.build();
    }

    @Override
    public boolean isAddedByAugmentation() {
        return addedByAugmentation;
    }

    @Override
    public QName getNodeType() {
        return extension == null ? nodeType : extension.getQName();
    }

    @Override
    public String getNodeParameter() {
        return nodeParameter;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public ExtensionDefinition getExtensionDefinition() {
        return extension;
    }

    @Nonnull
    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public String toString() {
        final QName type = getNodeType();
        return String.valueOf(type.getNamespace()) + ":" + type.getLocalName() + " " + nodeParameter;
    }
}
