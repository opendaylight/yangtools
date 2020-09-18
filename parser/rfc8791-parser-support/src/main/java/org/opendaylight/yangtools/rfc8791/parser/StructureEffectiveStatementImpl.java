/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureSchemaNode;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.CopyableMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.SchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.UnknownSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;

final class StructureEffectiveStatementImpl extends WithTypedefNamespace<QName, StructureStatement>
        implements StructureEffectiveStatement, StructureSchemaNode, CopyableMixin<QName, StructureStatement>,
                   SchemaNodeMixin<StructureStatement>, UnknownSchemaNodeMixin<QName, StructureStatement>,
                   MustConstraintMixin<QName, StructureStatement>, DataNodeContainerMixin<QName, StructureStatement>,
                   AugmentationTargetMixin<QName, StructureStatement> {
    private final @NonNull QName argument;
    private final int flags;

    StructureEffectiveStatementImpl(final EffectiveStmtCtx.Current<QName, StructureStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags) {
        super(ctx.declared(), substatements);
        argument = ctx.getArgument();
        this.flags = flags;
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QName getNodeType() {
        return statementDefinition().getStatementName();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public StructureEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
