/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.Optional;
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

final class ContainerEffectiveStatementImpl extends AbstractEffectiveContainerSchemaNode<ContainerStatement>
        implements ContainerEffectiveStatement, DerivableSchemaNode {
    private static final VarHandle ACTIONS;
    private static final VarHandle NOTIFICATIONS;

    static {
        final Lookup lookup = MethodHandles.lookup();

        try {
            ACTIONS = lookup.findVarHandle(ContainerEffectiveStatementImpl.class, "actions", ImmutableSet.class);
            NOTIFICATIONS = lookup.findVarHandle(ContainerEffectiveStatementImpl.class, "notifications",
                ImmutableSet.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @Nullable ContainerSchemaNode original;
    private final boolean presence;

    @SuppressWarnings("unused")
    private volatile ImmutableSet<NotificationDefinition> notifications;
    @SuppressWarnings("unused")
    private volatile ImmutableSet<ActionDefinition> actions;

    ContainerEffectiveStatementImpl(
            final StmtContext<QName, ContainerStatement, EffectiveStatement<QName, ContainerStatement>> ctx) {
        super(ctx);
        original = (ContainerSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        presence = findFirstEffectiveSubstatement(PresenceEffectiveStatement.class).isPresent();
    }

    @Override
    public Optional<ContainerSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public ImmutableSet<ActionDefinition> getActions() {
        return derivedSet(ACTIONS, ActionDefinition.class);
    }

    @Override
    public ImmutableSet<NotificationDefinition> getNotifications() {
        return derivedSet(NOTIFICATIONS, NotificationDefinition.class);
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
