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
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class UnknownEffectiveStatementImpl extends EffectiveStatementBase<String, UnknownStatement<String>> implements
        UnknownSchemaNode {

    private boolean augmenting;
    private boolean addedByUses;
    private UnknownSchemaNode original;

    private QName maybeQNameArgument;
    private final SchemaPath path;
    private ExtensionDefinition extension;
    private String description;
    private String reference;
    private final Status status = Status.CURRENT;
    private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();
    private QName nodeType;
    private final String nodeParameter;

    public UnknownEffectiveStatementImpl(final StmtContext<String, UnknownStatement<String>, ?> ctx) {
        super(ctx);

        final StmtContext<?, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> extensionInit = ctx
                .getFromNamespace(ExtensionNamespace.class, ctx.getPublicDefinition().getStatementName());

        if (extensionInit == null) {
            extension = null;
            nodeType = ctx.getPublicDefinition().getArgumentName();
        } else {
            extension = (ExtensionEffectiveStatementImpl) extensionInit.buildEffective();
            nodeType = extension.getQName();
        }

        List<TypeOfCopy> copyTypesFromOriginal = ctx.getCopyHistory();

        if (copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_AUGMENTATION)) {
            augmenting = true;
        }
        if (copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        }
        if (copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES_AUGMENTATION)) {
            addedByUses = augmenting = true;
        }

        if (ctx.getOriginalCtx() != null) {
            original = (UnknownSchemaNode) ctx.getOriginalCtx().buildEffective();
        }

        // FIXME: Remove following section after fixing 4380
        if(original != null) {
            maybeQNameArgument = original.getQName();
        } else {
            try {
                maybeQNameArgument = Utils.qNameFromArgument(ctx, argument());
            } catch (IllegalArgumentException e) {
                maybeQNameArgument = nodeType;
            }
        }
        path = Utils.getSchemaPath(ctx.getParentContext()).createChild(maybeQNameArgument);
        nodeParameter = (ctx.rawStatementArgument() == null) ? "" : ctx.rawStatementArgument();

        // TODO init other fields (see Bug1412Test)

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                description = ((DescriptionEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof UnknownEffectiveStatementImpl) {
                unknownNodes.add((UnknownEffectiveStatementImpl) effectiveStatement);
            }
        }
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
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
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
