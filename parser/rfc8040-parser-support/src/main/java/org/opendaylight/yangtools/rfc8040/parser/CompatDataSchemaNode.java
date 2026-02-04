/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

record CompatDataSchemaNode(@NonNull YangDataEffectiveStatementImpl stmt) implements ContainerLike {
    CompatDataSchemaNode {
        requireNonNull(stmt);
    }

    @Override
    public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return stmt.getTypeDefinitions();
    }

    @Override
    public Collection<? extends DataSchemaNode> getChildNodes() {
        return stmt.getChildNodes();
    }

    @Override
    public Collection<? extends GroupingDefinition> getGroupings() {
        return stmt.getGroupings();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return stmt.getDataChildByName(name);
    }

    @Override
    public Collection<? extends UsesNode> getUses() {
        return stmt.filterEffectiveStatements(UsesNode.class);
    }

    @Override
    public Collection<AugmentationSchemaNode> getAvailableAugmentations() {
        return Set.of();
    }

    @Override
    public Optional<Boolean> effectiveConfig() {
        return Optional.empty();
    }

    @Override
    public QName getQName() {
        // Note: we could try harder via QName.create(), but let's be consistent instead
        throw new UnsupportedOperationException();
    }

    @Override
    public Status getStatus() {
        return stmt.findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }

    @Override
    public Optional<String> getDescription() {
        return stmt.findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
    }

    @Override
    public Optional<String> getReference() {
        return stmt.findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class);
    }

    @Override
    public boolean isAugmenting() {
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        return false;
    }

    @Override
    public Optional<QualifiedBound> getWhenCondition() {
        return Optional.empty();
    }

    @Override
    public Collection<NotificationDefinition> getNotifications() {
        return Set.of();
    }

    @Override
    public Collection<ActionDefinition> getActions() {
        return Set.of();
    }

    @Override
    public Collection<MustDefinition> getMustConstraints() {
        return Set.of();
    }
}
