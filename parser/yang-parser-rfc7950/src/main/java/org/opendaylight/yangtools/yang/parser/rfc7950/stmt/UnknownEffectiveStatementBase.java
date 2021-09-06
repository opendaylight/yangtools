/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveDocumentedNodeWithStatus;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class UnknownEffectiveStatementBase<A, D extends UnknownStatement<A>>
        extends AbstractEffectiveDocumentedNodeWithStatus<A, D> implements UnknownSchemaNode {

    private final boolean addedByUses;
    private final boolean augmenting;

    private final ExtensionDefinition extension;
    private final QName nodeType;
    private final String nodeParameter;

    protected UnknownEffectiveStatementBase(final Current<A, D> stmt,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt.argument(), stmt.declared(), substatements);

        final StmtContext<?, ExtensionStatement, ExtensionEffectiveStatement> extensionInit =
                stmt.getFromNamespace(ExtensionNamespace.class, stmt.publicDefinition().getStatementName());

        if (extensionInit == null) {
            extension = null;
            nodeType = stmt.publicDefinition().getStatementName();
        } else {
            final EffectiveStatement<QName, ExtensionStatement> effective = extensionInit.buildEffective();
            checkState(effective instanceof ExtensionDefinition,
                "Statement %s is not an ExtensionDefinition", effective);
            extension = (ExtensionDefinition) effective;
            nodeType = null;
        }

        // initCopyType
        final CopyableNode copyTypesFromOriginal = stmt.history();
        this.augmenting = copyTypesFromOriginal.isAugmenting();
        this.addedByUses = copyTypesFromOriginal.isAddedByUses();

        nodeParameter = stmt.rawArgument() == null ? "" : stmt.rawArgument();
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

    @Deprecated
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
