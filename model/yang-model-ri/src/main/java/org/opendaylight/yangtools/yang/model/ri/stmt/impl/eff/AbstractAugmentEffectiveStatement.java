/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.QNameModuleAware;
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

abstract class AbstractAugmentEffectiveStatement extends DefaultWithSchemaTree<SchemaNodeIdentifier, AugmentStatement>
        implements AugmentEffectiveStatement, AugmentationSchemaNode, QNameModuleAware,
            DocumentedNodeMixin.WithStatus<SchemaNodeIdentifier, AugmentStatement>,
            DataNodeContainerMixin<SchemaNodeIdentifier, AugmentStatement>,
            ActionNodeContainerMixin<SchemaNodeIdentifier, AugmentStatement>,
            NotificationNodeContainerMixin<SchemaNodeIdentifier, AugmentStatement>,
            WhenConditionMixin<SchemaNodeIdentifier, AugmentStatement> {
    AbstractAugmentEffectiveStatement(final AugmentStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }

    @Override
    public final DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public final AugmentEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
