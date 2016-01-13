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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class ListEffectiveStatementImpl extends AbstractEffectiveSimpleDataNodeContainer<ListStatement> implements
        ListSchemaNode, DerivableSchemaNode {

    private final boolean userOrdered;
    private final List<QName> keyDefinition;
    private static final String ORDER_BY_USER_KEYWORD = "user";
    private final ListSchemaNode original;

    public ListEffectiveStatementImpl(
            final StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {
        super(ctx);

        this.original = ctx.getOriginalCtx() == null ? null : (ListSchemaNode) ctx.getOriginalCtx().buildEffective();

        OrderedByEffectiveStatementImpl orderedByStmt = firstEffective(OrderedByEffectiveStatementImpl.class);
        if (orderedByStmt != null && orderedByStmt.argument().equals(ORDER_BY_USER_KEYWORD)) {
            this.userOrdered = true;
        } else {
            this.userOrdered = false;
        }

        // initKeyDefinition
        List<QName> keyDefinitionInit = new LinkedList<>();
        KeyEffectiveStatementImpl keyEffectiveSubstatement = firstEffective(KeyEffectiveStatementImpl.class);

        if (keyEffectiveSubstatement != null) {
            Set<QName> possibleLeafQNamesForKey = new HashSet<>();

            for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
                if (effectiveStatement instanceof LeafSchemaNode) {
                    possibleLeafQNamesForKey.add(((LeafSchemaNode) effectiveStatement).getQName());
                }
            }

            Collection<SchemaNodeIdentifier> keys = keyEffectiveSubstatement.argument();
            for (SchemaNodeIdentifier key : keys) {
                final QName keyQName = key.getLastComponent();

                if (!possibleLeafQNamesForKey.contains(keyQName)) {
                    throw new InferenceException(String.format("Key '%s' misses node '%s' in list '%s'",
                            keyEffectiveSubstatement.getDeclared().rawArgument(), keyQName.getLocalName(),
                            ctx.getStatementArgument()), ctx.getStatementSourceReference());
                }

                keyDefinitionInit.add(keyQName);
            }
        }

        this.keyDefinition = ImmutableList.copyOf(keyDefinitionInit);
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
