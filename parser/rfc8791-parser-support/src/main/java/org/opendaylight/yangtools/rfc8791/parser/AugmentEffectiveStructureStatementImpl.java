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
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureArgument;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureSchemaNode;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.QNameModuleAware;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin.WithStatus;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.WhenConditionMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;

final class AugmentEffectiveStructureStatementImpl
        extends DefaultWithDataTree<AugmentStructureArgument, AugmentStructureStatement>
        implements AugmentStructureEffectiveStatement, WithStatus<AugmentStructureArgument, AugmentStructureStatement>,
                   DataNodeContainerMixin<AugmentStructureArgument, AugmentStructureStatement>,
                   ActionNodeContainerMixin<AugmentStructureArgument, AugmentStructureStatement>,
                   NotificationNodeContainerMixin<AugmentStructureArgument, AugmentStructureStatement>,
                   WhenConditionMixin<AugmentStructureArgument, AugmentStructureStatement>, AugmentStructureSchemaNode,
                   MustConstraintMixin<AugmentStructureArgument, AugmentStructureStatement>, QNameModuleAware {
    private final @NonNull AugmentStructureArgument argument;
    private final @NonNull QNameModule qnameModule;
    private final int flags;

    AugmentEffectiveStructureStatementImpl(
            final EffectiveStmtCtx.Current<AugmentStructureArgument, AugmentStructureStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?,?>> substatements, final int flags) {
        super(stmt.declared(), substatements);
        argument = stmt.getArgument();
        this.flags = flags;
        qnameModule = stmt.moduleName().getModule();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return getChildNodes().stream().filter(a -> a.getQName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    public AugmentStructureEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
