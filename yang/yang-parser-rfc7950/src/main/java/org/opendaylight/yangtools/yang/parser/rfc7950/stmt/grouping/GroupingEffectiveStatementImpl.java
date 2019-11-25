/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping;

import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class GroupingEffectiveStatementImpl
        extends AbstractEffectiveDocumentedDataNodeContainer<QName, GroupingStatement>
        implements GroupingDefinition, GroupingEffectiveStatement,
            ActionNodeContainerCompat<QName, GroupingStatement>,
            NotificationNodeContainerCompat<QName, GroupingStatement> {
    private static final VarHandle ACTIONS;
    private static final VarHandle NOTIFICATIONS;

    static {
        final Lookup lookup = MethodHandles.lookup();

        try {
            ACTIONS = lookup.findVarHandle(GroupingEffectiveStatementImpl.class, "actions", ImmutableSet.class);
            NOTIFICATIONS = lookup.findVarHandle(GroupingEffectiveStatementImpl.class, "notifications",
                ImmutableSet.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull QName qname;
    private final @NonNull SchemaPath path;
    private final boolean addedByUses;

    private volatile ImmutableSet<ActionDefinition> actions;
    private volatile ImmutableSet<NotificationDefinition> notifications;

    GroupingEffectiveStatementImpl(
            final StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> ctx) {
        super(ctx);

        qname = ctx.coerceStatementArgument();
        path = ctx.getSchemaPath().get();
        addedByUses = ctx.getCopyHistory().contains(CopyType.ADDED_BY_USES);
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(path);
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
        final GroupingEffectiveStatementImpl other = (GroupingEffectiveStatementImpl) obj;
        return Objects.equals(qname, other.qname) && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        return GroupingEffectiveStatementImpl.class.getSimpleName() + "[" + "qname=" + qname + "]";
    }
}
