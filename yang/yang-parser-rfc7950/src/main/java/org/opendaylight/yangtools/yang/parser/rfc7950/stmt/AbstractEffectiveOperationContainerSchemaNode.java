/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Deprecated(forRemoval = true)
public abstract class AbstractEffectiveOperationContainerSchemaNode<D extends DeclaredStatement<QName>>
        extends AbstractEffectiveContainerSchemaNode<D> {
    protected AbstractEffectiveOperationContainerSchemaNode(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
    }

    @Override
    public final Collection<? extends ActionDefinition> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public final Collection<? extends NotificationDefinition> getNotifications() {
        return ImmutableSet.of();
    }

    @Override
    public final boolean isPresenceContainer() {
        // FIXME: this should not really be here
        return false;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("path", getPath()).toString();
    }
}
