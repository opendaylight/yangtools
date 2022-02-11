/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.UserOrderedAwareMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.WhenConditionMixin;

abstract class AbstractListEffectiveStatement
        extends WithTypedefNamespace<QName, ListStatement, ListEffectiveStatement>
        implements ListEffectiveStatement, ListSchemaNode,
            ActionNodeContainerCompat<QName, ListStatement, ListEffectiveStatement>,
            NotificationNodeContainerCompat<QName, ListStatement, ListEffectiveStatement>,
            DataSchemaNodeMixin<ListStatement>, UserOrderedAwareMixin<QName, ListStatement, ListEffectiveStatement>,
            DataNodeContainerMixin<QName, ListStatement>, WhenConditionMixin<QName, ListStatement>,
            AugmentationTargetMixin<QName, ListStatement>, NotificationNodeContainerMixin<QName, ListStatement>,
            ActionNodeContainerMixin<QName, ListStatement>, MustConstraintMixin<QName, ListStatement> {
    private final int flags;
    private final @NonNull Object keyDefinition;

    AbstractListEffectiveStatement(final ListStatement declared, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<QName> keyDefinition) {
        super(declared, substatements);
        this.keyDefinition = maskList(keyDefinition);
        this.flags = flags;
    }

    AbstractListEffectiveStatement(final AbstractListEffectiveStatement original, final int flags) {
        super(original);
        keyDefinition = original.keyDefinition;
        this.flags = flags;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final List<QName> getKeyDefinition() {
        return unmaskList(keyDefinition, QName.class);
    }

    @Override
    public final DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public final Collection<? extends UniqueEffectiveStatement> getUniqueConstraints() {
        return effectiveSubstatements().stream()
                .filter(UniqueEffectiveStatement.class::isInstance)
                .map(UniqueEffectiveStatement.class::cast)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public final ListEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
