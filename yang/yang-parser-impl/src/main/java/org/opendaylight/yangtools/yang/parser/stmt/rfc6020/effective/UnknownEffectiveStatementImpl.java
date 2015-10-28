/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public final class UnknownEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<String, UnknownStatement<String>>
        implements UnknownSchemaNode {

    private final boolean addedByUses;
    private final boolean addedByAugmentation;
    private final QName maybeQNameArgument;
    private final SchemaPath path;
    private final ExtensionDefinition extension;
    private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();
    private final QName nodeType;
    private final String nodeParameter;

    public UnknownEffectiveStatementImpl(final StmtContext<String, UnknownStatement<String>, ?> ctx) {
        super(ctx);

        final StmtContext<?, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> extensionInit = ctx
                .getAllFromNamespace(ExtensionNamespace.class).get(ctx.getPublicDefinition().getStatementName());

        if (extensionInit == null) {
            extension = null;
            nodeType = ctx.getPublicDefinition().getArgumentName();
        } else {
            extension = (ExtensionEffectiveStatementImpl) extensionInit.buildEffective();
            nodeType = extension.getQName();
        }

        // initCopyType
        List<TypeOfCopy> copyTypesFromOriginal = ctx.getCopyHistory();
        if (copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES_AUGMENTATION)) {
            this.addedByUses = this.addedByAugmentation = true;
        } else {
            this.addedByAugmentation = copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_AUGMENTATION);
            this.addedByUses = copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES);
        }

        // FIXME: Remove following section after fixing 4380
        final UnknownSchemaNode original = ctx.getOriginalCtx() == null ? null : (UnknownSchemaNode) ctx
                .getOriginalCtx().buildEffective();
        if (original != null) {
            this.maybeQNameArgument = original.getQName();
        } else {
            QName maybeQNameArgumentInit = null;
            try {
                maybeQNameArgumentInit = Utils.qNameFromArgument(ctx, argument());
            } catch (IllegalArgumentException e) {
                maybeQNameArgumentInit = nodeType;
            }
            this.maybeQNameArgument = maybeQNameArgumentInit;
        }
        path = Utils.getSchemaPath(ctx.getParentContext()).createChild(maybeQNameArgument);
        nodeParameter = (ctx.rawStatementArgument() == null) ? "" : ctx.rawStatementArgument();

        // TODO init other fields (see Bug1412Test)
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownEffectiveStatementImpl) {
                unknownNodes.add((UnknownEffectiveStatementImpl) effectiveStatement);
            }
        }
    }

    @Override
    public boolean isAddedByAugmentation() {
        return addedByAugmentation;
    }

    @Override
    public QName getNodeType() {
        return nodeType;
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

    @Override
    public QName getQName() {
        return maybeQNameArgument;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(maybeQNameArgument);
        result = prime * result + Objects.hashCode(path);
        result = prime * result + Objects.hashCode(nodeType);
        result = prime * result + Objects.hashCode(nodeParameter);
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
        UnknownEffectiveStatementImpl other = (UnknownEffectiveStatementImpl) obj;
        if (!Objects.equals(maybeQNameArgument, other.maybeQNameArgument)) {
            return false;
        }
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        if (!Objects.equals(nodeType, other.nodeType)) {
            return false;
        }
        if (!Objects.equals(nodeParameter, other.nodeParameter)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeType.getNamespace());
        sb.append(":");
        sb.append(nodeType.getLocalName());
        sb.append(" ");
        sb.append(nodeParameter);
        return sb.toString();
    }

}
