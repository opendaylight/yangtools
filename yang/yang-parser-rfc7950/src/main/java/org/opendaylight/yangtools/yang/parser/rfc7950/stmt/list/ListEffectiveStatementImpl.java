/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveMustConstraintAwareSimpleDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ListEffectiveStatementImpl
        extends AbstractEffectiveMustConstraintAwareSimpleDataNodeContainer<ListStatement>
        implements ListEffectiveStatement, ListSchemaNode, DerivableSchemaNode,
            ActionNodeContainerCompat<QName, ListStatement>, NotificationNodeContainerCompat<QName, ListStatement> {
    private static final String ORDER_BY_USER_KEYWORD = "user";
    private static final VarHandle ACTIONS;
    private static final VarHandle NOTIFICATIONS;

    static {
        final Lookup lookup = MethodHandles.lookup();

        try {
            ACTIONS = lookup.findVarHandle(ListEffectiveStatementImpl.class, "actions", ImmutableSet.class);
            NOTIFICATIONS = lookup.findVarHandle(ListEffectiveStatementImpl.class, "notifications",
                ImmutableSet.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final boolean userOrdered;
    private final ImmutableList<QName> keyDefinition;
    private final ListSchemaNode original;
    private final @NonNull ImmutableList<UniqueConstraint> uniqueConstraints;
    private final ElementCountConstraint elementCountConstraint;

    @SuppressWarnings("unused")
    private volatile ImmutableSet<NotificationDefinition> notifications;
    @SuppressWarnings("unused")
    private volatile ImmutableSet<ActionDefinition> actions;

    ListEffectiveStatementImpl(
            final StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {
        super(ctx);

        this.original = (ListSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        this.userOrdered = findFirstEffectiveSubstatementArgument(OrderedByEffectiveStatement.class)
                .map(ORDER_BY_USER_KEYWORD::equals).orElse(Boolean.FALSE).booleanValue();

        // initKeyDefinition
        final Optional<KeyEffectiveStatement> optKeyStmt = findFirstEffectiveSubstatement(KeyEffectiveStatement.class);
        if (optKeyStmt.isPresent()) {
            final KeyEffectiveStatement keyStmt = optKeyStmt.get();
            final List<QName> keyDefinitionInit = new ArrayList<>(keyStmt.argument().size());
            final Set<QName> possibleLeafQNamesForKey = new HashSet<>();
            for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
                if (effectiveStatement instanceof LeafSchemaNode) {
                    possibleLeafQNamesForKey.add(((LeafSchemaNode) effectiveStatement).getQName());
                }
            }
            for (final SchemaNodeIdentifier key : keyStmt.argument()) {
                final QName keyQName = key.getLastComponent();

                if (!possibleLeafQNamesForKey.contains(keyQName)) {
                    throw new InferenceException(ctx.getStatementSourceReference(),
                            "Key '%s' misses node '%s' in list '%s'", keyStmt.getDeclared().rawArgument(),
                            keyQName.getLocalName(), ctx.getStatementArgument());
                }
                keyDefinitionInit.add(keyQName);
            }

            this.keyDefinition = ImmutableList.copyOf(keyDefinitionInit);
        } else {
            this.keyDefinition = ImmutableList.of();
        }

        uniqueConstraints = ImmutableList.copyOf(allSubstatementsOfType(UniqueConstraint.class));
        elementCountConstraint = EffectiveStmtUtils.createElementCountConstraint(this).orElse(null);
    }

    @Override
    public Optional<ListSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public List<QName> getKeyDefinition() {
        return keyDefinition;
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
    public Collection<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }

    @Override
    public boolean isUserOrdered() {
        return userOrdered;
    }

    @Override
    public Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.ofNullable(elementCountConstraint);
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
