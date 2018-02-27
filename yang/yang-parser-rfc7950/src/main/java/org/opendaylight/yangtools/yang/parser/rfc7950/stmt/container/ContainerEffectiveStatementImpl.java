/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveContainerSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Internal implementation of ContainerEffectiveStatement.
 *
 * @deprecated This class is visible only for historical purposes and is going to be hidden.
 */
// FIXME: 3.0.0: hide this class
@Deprecated
public final class ContainerEffectiveStatementImpl extends AbstractEffectiveContainerSchemaNode<ContainerStatement>
        implements ContainerEffectiveStatement, DerivableSchemaNode {
    private final @NonNull ImmutableSet<ActionDefinition> actions;
    private final @NonNull ImmutableSet<NotificationDefinition> notifications;
    private final @Nullable ContainerSchemaNode original;
    private final boolean presence;

    ContainerEffectiveStatementImpl(
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
        presence = findFirstEffectiveSubstatement(PresenceEffectiveStatement.class).isPresent();
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
    public boolean isPresenceContainer() {
        return presence;
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
