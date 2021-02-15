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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.PresenceMixin;

public final class ContainerEffectiveStatementImpl
        extends WithSubstatements<QName, ContainerStatement, ContainerEffectiveStatement>
        implements ContainerEffectiveStatement, ContainerSchemaNode, DerivableSchemaNode,
            DataSchemaNodeMixin<QName, ContainerStatement>, DataNodeContainerMixin<QName, ContainerStatement>,
            ActionNodeContainerMixin<QName, ContainerStatement>,
            ActionNodeContainerCompat<QName, ContainerStatement, ContainerEffectiveStatement>,
            NotificationNodeContainerMixin<QName, ContainerStatement>,
            NotificationNodeContainerCompat<QName, ContainerStatement, ContainerEffectiveStatement>,
            MustConstraintMixin<QName, ContainerStatement>, PresenceMixin<QName, ContainerStatement>,
            AugmentationTargetMixin<QName, ContainerStatement> {

    private final int flags;
    private final @NonNull Immutable path;
    private final @Nullable ContainerSchemaNode original;

    public ContainerEffectiveStatementImpl(final ContainerStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final Immutable path,
            final int flags, final @Nullable ContainerSchemaNode original) {
        super(declared, substatements);
        this.path = requireNonNull(path);
        this.original = original;
        this.flags = flags;
    }

    public ContainerEffectiveStatementImpl(final ContainerEffectiveStatementImpl origEffective, final Immutable path,
            final int flags, final @Nullable ContainerSchemaNode original) {
        super(origEffective);
        this.path = requireNonNull(path);
        this.original = original;
        this.flags = flags;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QName argument() {
        return getQName();
    }

    @Override
    public Immutable pathObject() {
        return path;
    }

    @Override
    public Optional<ContainerSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
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
        return "container " + getQName().getLocalName();
    }
}
