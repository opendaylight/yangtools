/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

final class TargetAugmentEffectiveStatement implements AugmentEffectiveStatement, AugmentationSchemaNode {
    private final @NonNull List<EffectiveStatement<?, ?>> substatements;
    private final @NonNull AugmentEffectiveStatement delegate;
    private final @NonNull AugmentationSchemaNode schemaDelegate;

    TargetAugmentEffectiveStatement(final AugmentEffectiveStatement augment,
            final SchemaTreeAwareEffectiveStatement<?, ?> target,
            final ImmutableList<EffectiveStatement<?, ?>> substatements) {
        delegate = requireNonNull(augment);
        verify(augment instanceof AugmentationSchemaNode, "Unsupported augment implementation %s", augment);
        schemaDelegate = (AugmentationSchemaNode) augment;
        this.substatements = requireNonNull(substatements);
    }

    @NonNull AugmentEffectiveStatement delegate() {
        return delegate;
    }

    @Override
    public AugmentStatement declared() {
        return delegate.declared();
    }

    @Override
    public SchemaNodeIdentifier argument() {
        return delegate.argument();
    }

    @Override
    public StatementOrigin statementOrigin() {
        return delegate.statementOrigin();
    }

    @Override
    public List<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection<SchemaTreeEffectiveStatement<?>> schemaTreeNodes() {
        return (Collection) collectEffectiveSubstatements(SchemaTreeEffectiveStatement.class);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(final QName qname) {
        return (Optional) streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class)
            .filter(stmt -> qname.equals(stmt.argument()))
            .findAny();
    }

    @Override
    public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return schemaDelegate.getTypeDefinitions();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<? extends DataSchemaNode> getChildNodes() {
        return (Collection) Collections2.filter(substatements, DataSchemaNode.class::isInstance);
    }

    @Override
    public Collection<? extends GroupingDefinition> getGroupings() {
        return schemaDelegate.getGroupings();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return getChildNodes().stream().filter(child -> name.equals(child.getQName())).findFirst().orElse(null);
    }

    @Override
    public Collection<? extends UsesNode> getUses() {
        return schemaDelegate.getUses();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<? extends NotificationDefinition> getNotifications() {
        return (Collection) Collections2.filter(substatements, NotificationDefinition.class::isInstance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<? extends ActionDefinition> getActions() {
        return (Collection) Collections2.filter(substatements, ActionDefinition.class::isInstance);
    }

    @Override
    public Optional<? extends QualifiedBound> getWhenCondition() {
        return schemaDelegate.getWhenCondition();
    }

    @Override
    public @NonNull Status getStatus() {
        return schemaDelegate.getStatus();
    }

    @Override
    public Optional<String> getDescription() {
        return schemaDelegate.getDescription();
    }

    @Override
    public Optional<String> getReference() {
        return schemaDelegate.getReference();
    }

    @Override
    public AugmentEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("delegate", delegate)
            .add("substatements", substatements.size())
            .toString();
    }
}
