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
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AddedByUsesMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.SchemaNodeMixin;

public abstract class AbstractGroupingEffectiveStatement extends WithTypedefNamespace<QName, @NonNull GroupingStatement>
        implements GroupingDefinition, GroupingEffectiveStatement,
            DataNodeContainerMixin<QName, @NonNull GroupingStatement>, SchemaNodeMixin<@NonNull GroupingStatement>,
            ActionNodeContainerMixin<QName, @NonNull GroupingStatement>,
            NotificationNodeContainerMixin<QName, @NonNull GroupingStatement>,
            AddedByUsesMixin<QName, @NonNull GroupingStatement> {
    private final int flags;

    AbstractGroupingEffectiveStatement(final GroupingStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags) {
        super(declared, substatements);
        this.flags = flags;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public final GroupingEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
