/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithSchemaTree.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.AddedByUsesMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.SchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class GroupingEffectiveStatementImpl
        extends WithSubstatements<QName, GroupingStatement, GroupingEffectiveStatement>
        implements GroupingDefinition, GroupingEffectiveStatement,
            DataNodeContainerMixin<QName, GroupingStatement>,
            SchemaNodeMixin<QName, GroupingStatement>, ActionNodeContainerMixin<QName, GroupingStatement>,
            NotificationNodeContainerMixin<QName, GroupingStatement>, AddedByUsesMixin<QName, GroupingStatement> {
    private final @NonNull SchemaPath path;
    private final int flags;

    GroupingEffectiveStatementImpl(final GroupingStatement declared, final StmtContext<?, ?, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags) {
        super(declared, ctx, substatements);
        this.path = ctx.getSchemaPath().get();
        this.flags = flags;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public @Nullable QName argument() {
        return getQName();
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return findDataSchemaNode(name);
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
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
