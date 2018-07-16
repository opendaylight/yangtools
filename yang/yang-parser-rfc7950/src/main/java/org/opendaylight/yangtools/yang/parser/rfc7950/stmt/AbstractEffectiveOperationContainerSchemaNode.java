/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class AbstractEffectiveOperationContainerSchemaNode<D extends DeclaredStatement<QName>>
        extends AbstractEffectiveContainerSchemaNode<D> {
    protected AbstractEffectiveOperationContainerSchemaNode(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
    }

    @Override
    public final Set<ActionDefinition> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public final Set<NotificationDefinition> getNotifications() {
        return ImmutableSet.of();
    }

    @Override
    public final boolean isPresenceContainer() {
        // FIXME: this should not really be here
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getQName(), getPath());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractEffectiveOperationContainerSchemaNode<?> other = (AbstractEffectiveOperationContainerSchemaNode<?>) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("path", getPath()).toString();
    }
}
