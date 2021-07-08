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
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.CopyableMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin.WithStatus;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;

public final class NotificationEffectiveStatementImpl
        extends WithSubstatements<QName, NotificationStatement, NotificationEffectiveStatement>
        implements NotificationDefinition, NotificationEffectiveStatement,
                    WithStatus<QName, NotificationStatement>, DataNodeContainerMixin<QName, NotificationStatement>,
                   AugmentationTargetMixin<QName, NotificationStatement>, CopyableMixin<QName, NotificationStatement>,
                   MustConstraintMixin<QName, NotificationStatement> {

    private final @NonNull QName qname;
    private final int flags;

    public NotificationEffectiveStatementImpl(final NotificationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName qname,
            final int flags) {
        super(declared, substatements);
        this.qname = requireNonNull(qname);
        this.flags = flags;
    }

    public NotificationEffectiveStatementImpl(final NotificationEffectiveStatementImpl original, final QName qname,
            final int flags) {
        super(original);
        this.qname = requireNonNull(qname);
        this.flags = flags;
    }

    @Override
    public QName argument() {
        return qname;
    }

    @Override
    public QName getQName() {
        return qname;
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

    @Override
    public String toString() {
        return NotificationEffectiveStatementImpl.class.getSimpleName() + "[qname=" + qname + "]";
    }
}
