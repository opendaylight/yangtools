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
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithSchemaTree;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.WhenConditionMixin;

public final class AugmentEffectiveStatementImpl
        extends DefaultWithSchemaTree<SchemaNodeIdentifier, @NonNull AugmentStatement>
        implements AugmentEffectiveStatement, AugmentationSchemaNode,
            DocumentedNodeMixin.WithStatus<SchemaNodeIdentifier, @NonNull AugmentStatement>,
            DataNodeContainerMixin<SchemaNodeIdentifier, @NonNull AugmentStatement>,
            ActionNodeContainerMixin<SchemaNodeIdentifier, @NonNull AugmentStatement>,
            NotificationNodeContainerMixin<SchemaNodeIdentifier, @NonNull AugmentStatement>,
            WhenConditionMixin<SchemaNodeIdentifier, @NonNull AugmentStatement> {
    private final @NonNull SchemaNodeIdentifier argument;
    private final int flags;

    public AugmentEffectiveStatementImpl(final @NonNull AugmentStatement declared,
            final SchemaNodeIdentifier argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    @Override
    public @NonNull SchemaNodeIdentifier argument() {
        return argument;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public AugmentEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
