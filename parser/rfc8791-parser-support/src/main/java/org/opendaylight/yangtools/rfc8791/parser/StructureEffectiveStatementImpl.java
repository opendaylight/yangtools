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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureSchemaNode;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin.WithStatus;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.UnknownSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;

final class StructureEffectiveStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<QName, StructureStatement>
        implements StructureEffectiveStatement, StructureSchemaNode, WithStatus<QName, StructureStatement>,
                   MustConstraintMixin<QName, StructureStatement>, UnknownSchemaNodeMixin<QName, StructureStatement>,
                   DataNodeContainerMixin<QName, StructureStatement>, ContainerLike,
        ActionNodeContainerMixin<QName, StructureStatement>,
        NotificationNodeContainerMixin<QName, StructureStatement>,
        AugmentationTargetMixin<QName, StructureStatement>,
        DataSchemaNodeMixin<StructureStatement> {
    private final int flag;

    StructureEffectiveStatementImpl(final EffectiveStmtCtx.Current<QName, StructureStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flag) {
        super(ctx.declared(), ctx.getArgument(), ctx.history(), substatements);
        this.flag = flag;
    }

    @Override
    public @NonNull QName getQName() {
        return super.argument();
    }

    @Override
    public @Nullable DataSchemaNode dataChildByName(QName name) {
        return getChildNodes().stream().filter(a -> a.getQName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public int flags() {
        return flag;
    }

    @Override
    public @NonNull UnknownEffectiveStatement<?, ?> asEffectiveStatement() {
        return this;
    }
}
