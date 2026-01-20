/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

@NonNullByDefault
final class YangDataEffectiveStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<YangDataName, YangDataStatement>
        implements YangDataEffectiveStatement, YangDataSchemaNode,
                   DataNodeContainerMixin<YangDataName, YangDataStatement> {
    private final DataSchemaNode child;

    YangDataEffectiveStatementImpl(final Current<YangDataName, YangDataStatement> stmt,
             final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final DataSchemaNode child) {
        super(stmt.declared(), stmt.getArgument(), stmt.history(), substatements);
        this.child = requireNonNull(child);
    }

    @Override
    public YangDataName name() {
        return argument();
    }

    @Override
    public QName getQName() {
        return child.getQName();
    }

    @Override
    public @Nullable DataSchemaNode dataChildByName(final @Nullable QName name) {
        // FIXME: dataChildByName() should be fixed
        return requireNonNull(name).equals(child.getQName()) ? child : null;
    }

    @Override
    public YangDataEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public Optional<DataTreeEffectiveStatement<?>> findDataTreeNode(final QName qname) {
        if (child instanceof DataTreeEffectiveStatement<?> dataChild && dataChild.argument().equals(qname)) {
            return Optional.of(dataChild);
        }
        if (child instanceof DataTreeAwareEffectiveStatement<?, ?> aware) {
            // A schema tree statement which *has to* know about data tree -- just forward it
            return aware.findDataTreeNode(qname);
        }
        throw new VerifyException("Unexpected child " + child);
    }

    @Override
    public Collection<DataTreeEffectiveStatement<?>> dataTreeNodes() {
        if (child instanceof DataTreeEffectiveStatement<?> dataChild) {
            return List.of(dataChild);
        }
        if (child instanceof DataTreeAwareEffectiveStatement<?, ?> aware) {
            // A schema tree statement which *has to* know about data tree -- just forward it
            return aware.dataTreeNodes();
        }
        throw new VerifyException("Unexpected child " + child);
    }

    @Override
    public Collection<SchemaTreeEffectiveStatement<?>> schemaTreeNodes() {
        return (Collection) List.of(child);
    }

    @Override
    public Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(final QName qname) {
        return qname.equals(child.getQName()) ? (Optional) Optional.of(child) : Optional.empty();
    }
}
