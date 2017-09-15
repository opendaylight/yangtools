/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class GroupingEffectiveStatementImpl extends
        AbstractEffectiveDocumentedDataNodeContainer<QName, GroupingStatement> implements GroupingDefinition {
    private final QName qname;
    private final SchemaPath path;
    private final boolean addedByUses;
    private final Set<ActionDefinition> actions;
    private final Set<NotificationDefinition> notifications;
    private final List<UnknownSchemaNode> unknownNodes;

    public GroupingEffectiveStatementImpl(
            final StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> ctx) {
        super(ctx);

        qname = ctx.getStatementArgument();
        path = ctx.getSchemaPath().get();
        addedByUses = ctx.getCopyHistory().contains(CopyType.ADDED_BY_USES);

        final ImmutableSet.Builder<ActionDefinition> actionsBuilder = ImmutableSet.builder();
        final ImmutableSet.Builder<NotificationDefinition> notificationsBuilder = ImmutableSet.builder();
        final ImmutableList.Builder<UnknownSchemaNode> b = ImmutableList.builder();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof ActionDefinition) {
                actionsBuilder.add((ActionDefinition) effectiveStatement);
            }

            if (effectiveStatement instanceof NotificationDefinition) {
                notificationsBuilder.add((NotificationDefinition) effectiveStatement);
            }

            if (effectiveStatement instanceof UnknownSchemaNode) {
                b.add((UnknownSchemaNode) effectiveStatement);
            }
        }

        this.actions = actionsBuilder.build();
        this.notifications = notificationsBuilder.build();
        unknownNodes = b.build();
    }

    @Nonnull
    @Override
    public QName getQName() {
        return qname;
    }

    @Nonnull
    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public Set<ActionDefinition> getActions() {
        return actions;
    }

    @Nonnull
    @Override
    public Set<NotificationDefinition> getNotifications() {
        return notifications;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
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
