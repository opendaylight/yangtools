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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AddedByUsesMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.SchemaNodeMixin;

public final class GroupingEffectiveStatementImpl
        extends WithSubstatements<QName, GroupingStatement, GroupingEffectiveStatement>
        implements GroupingDefinition, GroupingEffectiveStatement,
            DataNodeContainerMixin<QName, GroupingStatement>,
            SchemaNodeMixin<QName, GroupingStatement>, ActionNodeContainerMixin<QName, GroupingStatement>,
            NotificationNodeContainerMixin<QName, GroupingStatement>, AddedByUsesMixin<QName, GroupingStatement> {
    private final @NonNull Immutable path;
    private final int flags;

    public GroupingEffectiveStatementImpl(final GroupingStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final Immutable path,
            final int flags) {
        super(declared, substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public Immutable pathObject() {
        return path;
    }

    @Override
    public QName argument() {
        return getQName();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public GroupingEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return GroupingEffectiveStatementImpl.class.getSimpleName() + "[" + "qname=" + getQName() + "]";
    }
}
