/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class ListEffectiveStatementImpl extends AbstractEffectiveSimpleDataNodeContainer<ListStatement> implements
        ListSchemaNode, DerivableSchemaNode {
    private static final String ORDER_BY_USER_KEYWORD = "user";

    private final boolean userOrdered;
    private final List<QName> keyDefinition;
    private final ListSchemaNode original;
    private final Set<ActionDefinition> actions;
    private final Set<NotificationDefinition> notifications;
    private final Collection<UniqueConstraint> uniqueConstraints;

    public ListEffectiveStatementImpl(
            final StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {
        super(ctx);

        this.original = (ListSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        final OrderedByEffectiveStatementImpl orderedByStmt = firstEffective(OrderedByEffectiveStatementImpl.class);
        if (orderedByStmt != null && ORDER_BY_USER_KEYWORD.equals(orderedByStmt.argument())) {
            this.userOrdered = true;
        } else {
            this.userOrdered = false;
        }

        // initKeyDefinition
        final List<QName> keyDefinitionInit = new LinkedList<>();
        final KeyEffectiveStatementImpl keyEffectiveSubstatement = firstEffective(KeyEffectiveStatementImpl.class);
        if (keyEffectiveSubstatement != null) {
            final Set<QName> possibleLeafQNamesForKey = new HashSet<>();
            for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
                if (effectiveStatement instanceof LeafSchemaNode) {
                    possibleLeafQNamesForKey.add(((LeafSchemaNode) effectiveStatement).getQName());
                }
            }
            for (final SchemaNodeIdentifier key : keyEffectiveSubstatement.argument()) {
                final QName keyQName = key.getLastComponent();

                if (!possibleLeafQNamesForKey.contains(keyQName)) {
                    throw new InferenceException(ctx.getStatementSourceReference(),
                            "Key '%s' misses node '%s' in list '%s'", keyEffectiveSubstatement.getDeclared()
                                    .rawArgument(), keyQName.getLocalName(), ctx.getStatementArgument());
                }
                keyDefinitionInit.add(keyQName);
            }
        }
        this.keyDefinition = ImmutableList.copyOf(keyDefinitionInit);
        this.uniqueConstraints = ImmutableList.copyOf(allSubstatementsOfType(UniqueConstraint.class));

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
    public Optional<ListSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public List<QName> getKeyDefinition() {
        return keyDefinition;
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
    @Nonnull
    public Collection<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }

    @Override
    public boolean isUserOrdered() {
        return userOrdered;
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
        final ListEffectiveStatementImpl other = (ListEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return "list " + getQName().getLocalName();
    }
}
