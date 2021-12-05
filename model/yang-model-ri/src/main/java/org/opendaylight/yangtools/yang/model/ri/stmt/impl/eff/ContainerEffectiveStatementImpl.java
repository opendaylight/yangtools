/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

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

public final class ContainerEffectiveStatementImpl
        extends WithTypedefNamespace<QName, ContainerStatement, ContainerEffectiveStatement>
        implements ContainerEffectiveStatement, ContainerSchemaNode, DataSchemaNodeMixin<ContainerStatement>,
            DataNodeContainerMixin<QName, ContainerStatement>, ActionNodeContainerMixin<QName, ContainerStatement>,
            ActionNodeContainerCompat<QName, ContainerStatement, ContainerEffectiveStatement>,
            NotificationNodeContainerMixin<QName, ContainerStatement>,
            NotificationNodeContainerCompat<QName, ContainerStatement, ContainerEffectiveStatement>,
            MustConstraintMixin<QName, ContainerStatement>, PresenceMixin<QName, ContainerStatement>,
            AugmentationTargetMixin<QName, ContainerStatement> {

    private final int flags;
    private final @NonNull QName argument;

    public ContainerEffectiveStatementImpl(final ContainerStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName argument,
            final int flags) {
        super(declared, substatements);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    public ContainerEffectiveStatementImpl(final ContainerEffectiveStatementImpl origEffective, final QName argument,
            final int flags) {
        super(origEffective);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public boolean isPresenceContainer() {
        return presence();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public ContainerEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return "container " + argument.getLocalName();
    }
}
