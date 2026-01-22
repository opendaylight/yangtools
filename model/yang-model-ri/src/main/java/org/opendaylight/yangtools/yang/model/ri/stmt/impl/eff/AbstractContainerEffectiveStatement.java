/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.PresenceMixin;

public abstract class AbstractContainerEffectiveStatement
        extends WithTypedefNamespace<QName, @NonNull ContainerStatement>
        implements ContainerEffectiveStatement, ContainerSchemaNode, DataSchemaNodeMixin<@NonNull ContainerStatement>,
            DataNodeContainerMixin<QName, @NonNull ContainerStatement>,
            ActionNodeContainerMixin<QName, @NonNull ContainerStatement>,
            ActionNodeContainerCompat<QName, @NonNull ContainerStatement, ContainerEffectiveStatement>,
            NotificationNodeContainerMixin<QName, @NonNull ContainerStatement>,
            NotificationNodeContainerCompat<QName, @NonNull ContainerStatement, ContainerEffectiveStatement>,
            MustConstraintMixin<QName, @NonNull ContainerStatement>, PresenceMixin<QName, @NonNull ContainerStatement>,
            AugmentationTargetMixin<QName, @NonNull ContainerStatement> {
    private final int flags;

    AbstractContainerEffectiveStatement(final @NonNull ContainerStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags) {
        super(declared, substatements);
        this.flags = flags;
    }

    AbstractContainerEffectiveStatement(final AbstractContainerEffectiveStatement origEffective, final int flags) {
        super(origEffective);
        this.flags = flags;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final boolean isPresenceContainer() {
        return presence();
    }

    @Override
    public final DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public final ContainerEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
