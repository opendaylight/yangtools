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
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureSchemaNode;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.QNameModuleAware;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin.WithStatus;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.WhenConditionMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;

public class AugmentEffectiveStructureStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<Absolute, AugmentStructureStatement>
        implements AugmentStructureEffectiveStatement, WithStatus<Absolute, AugmentStructureStatement>,
        DataNodeContainerMixin<Absolute, AugmentStructureStatement>,
        ActionNodeContainerMixin<Absolute, AugmentStructureStatement>,
        NotificationNodeContainerMixin<Absolute, AugmentStructureStatement>,
        WhenConditionMixin<Absolute, AugmentStructureStatement>, AugmentStructureSchemaNode,
        MustConstraintMixin<Absolute, AugmentStructureStatement>, QNameModuleAware {
    final int flag;
    final QNameModule qnameModule;

    public AugmentEffectiveStructureStatementImpl(EffectiveStmtCtx.Current<Absolute, AugmentStructureStatement> stmt,
            ImmutableList<? extends EffectiveStatement<?,?>> substatements, final int flag) {
        super(stmt.declared(), stmt.getArgument(), stmt.history(), substatements);
        this.flag = flag;
        this.qnameModule = stmt.moduleName().getModule();
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
    public @NonNull QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    public @NonNull QName getQName() {
        return getNodeType();
    }

    @Override
    public @NonNull UnknownEffectiveStatement<?, ?> asEffectiveStatement() {
        return this;
    }
}
