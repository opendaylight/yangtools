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
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.CopyableMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.SchemaNodeMixin;

public final class NotificationEffectiveStatementImpl
        extends WithTypedefNamespace<QName, @NonNull NotificationStatement>
        implements NotificationDefinition, NotificationEffectiveStatement,
                   SchemaNodeMixin<@NonNull NotificationStatement>,
                   DataNodeContainerMixin<QName, @NonNull NotificationStatement>,
                   AugmentationTargetMixin<QName, @NonNull NotificationStatement>,
                   CopyableMixin<QName, @NonNull NotificationStatement>,
                   MustConstraintMixin<QName, @NonNull NotificationStatement> {

    private final @NonNull QName argument;
    private final int flags;

    public NotificationEffectiveStatementImpl(final @NonNull NotificationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName argument,
            final int flags) {
        super(declared, substatements);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    public NotificationEffectiveStatementImpl(final NotificationEffectiveStatementImpl original, final QName argument,
            final int flags) {
        super(original);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public NotificationEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
