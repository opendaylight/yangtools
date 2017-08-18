/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class ContainerEffectiveStatementImpl extends AbstractEffectiveContainerSchemaNode<ContainerStatement> implements
        DerivableSchemaNode {
    private final Set<ActionDefinition> actions;
    private final Set<NotificationDefinition> notifications;
    private final ContainerSchemaNode original;

    public ContainerEffectiveStatementImpl(
            final StmtContext<QName, ContainerStatement, EffectiveStatement<QName, ContainerStatement>> ctx) {
        super(ctx);
        this.original = (ContainerSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        final ImmutableSet.Builder<ActionDefinition> actionsBuilder = ImmutableSet.builder();
        final Builder<NotificationDefinition> notificationsBuilder = ImmutableSet.builder();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof ActionDefinition) {
                actionsBuilder.add((ActionDefinition) effectiveStatement);
            }

            if (effectiveStatement instanceof NotificationDefinition) {
                notificationsBuilder.add((NotificationDefinition) effectiveStatement);
            }
        }

        this.actions = actionsBuilder.build();
        this.notifications = notificationsBuilder.build();
    }

    @Override
    public Optional<ContainerSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public Set<ActionDefinition> getActions() {
        return actions;
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        return notifications;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(getPath());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContainerEffectiveStatementImpl other = (ContainerEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return "container " + getQName().getLocalName();
    }
}
