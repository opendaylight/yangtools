/*
 * Copyright (c) 2017 OpenDaylight.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.IfFeatureStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Objects;
import java.util.Optional;

public class ListEffectiveStatementRfc7950Impl extends AbstractEffectiveSimpleDataNodeContainer<ListStatement> implements
        ListSchemaNode, DerivableSchemaNode {

    private static final String ORDER_BY_USER_KEYWORD = "user";

    private final boolean userOrdered;
    private final List<QName> keyDefinition;
    private final ListSchemaNode original;
    private final Set<ActionDefinition> actions;
    private final Set<NotificationDefinition> notifications;
    private final Collection<UniqueConstraint> uniqueConstraints;

    public ListEffectiveStatementRfc7950Impl (
            final StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {
        super(ctx);

        this.original = ctx.getOriginalCtx() == null ? null : (ListSchemaNode) ctx.getOriginalCtx().buildEffective();

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
                    effectiveStatement.effectiveSubstatements().forEach(leafSubstatement -> {
                        if ((leafSubstatement instanceof IfFeatureEffectiveStatementImpl)
                                || (leafSubstatement instanceof WhenEffectiveStatementImpl)) {
                            throw new InferenceException(ctx.getStatementSourceReference(),
                                    "For Yang 1.1 the sub-statements 'when' and 'if-feature' are illegal on list keys");
                        }
                    });
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
        final ImmutableSet.Builder<NotificationDefinition> notificationsBuilder = ImmutableSet.builder();
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
    public com.google.common.base.Optional<ListSchemaNode> getOriginal() {
        return com.google.common.base.Optional.fromNullable(original);
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
