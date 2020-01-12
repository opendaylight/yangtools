/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementFlags.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementFlags.UserOrderedMixin;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

abstract class AbstractListEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.DefaultWithDataTree<QName, ListStatement, ListEffectiveStatement>
        implements ListEffectiveStatement, ListSchemaNode, DerivableSchemaNode,
            ActionNodeContainerCompat<QName, ListStatement>, NotificationNodeContainerCompat<QName, ListStatement>,
            DataSchemaNodeMixin<QName, ListStatement>, UserOrderedMixin {
    private final int flags;
    private final @NonNull Object substatements;
    private final @NonNull SchemaPath path;

    AbstractListEffectiveStatement(final ListStatement declared,  final SchemaPath path, final int flags,
            final StatementSourceReference ref, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, ref, substatements);
        this.substatements = substatements.size() == 1 ? substatements.get(0) : substatements;
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        if (substatements instanceof ImmutableList) {
            return (ImmutableList<? extends EffectiveStatement<?, ?>>) substatements;
        }
        verify(substatements instanceof EffectiveStatement, "Unexpected substatement %s", substatements);
        return ImmutableList.of((EffectiveStatement<?, ?>) substatements);
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final @NonNull QName argument() {
        return getQName();
    }

    @Override
    public final SchemaPath getPath() {
        return path;
    }

    @Override
    public final boolean isUserOrdered() {
        return userOrdered();
    }

    @Override
    public final Collection<DataSchemaNode> getChildNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final Optional<DataSchemaNode> findDataChildByName(final QName name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final Optional<RevisionAwareXPath> getWhenCondition() {
        return findFirstEffectiveSubstatementArgument(WhenEffectiveStatement.class);
    }

    @Override
    public final Set<UsesNode> getUses() {
        return getSubstatements(UsesNode.class);
    }

    @Override
    public final Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return getSubstatements(AugmentationSchemaNode.class);
    }

    @Override
    public final Collection<UniqueConstraint> getUniqueConstraints() {
        return streamSubstatements(UniqueConstraint.class).collect(ImmutableList.toImmutableList());
    }

    @Override
    public final Set<NotificationDefinition> getNotifications() {
        return getSubstatements(NotificationDefinition.class);
    }

    @Override
    public final Set<ActionDefinition> getActions() {
        return getSubstatements(ActionDefinition.class);
    }

    @Override
    public final Collection<MustDefinition> getMustConstraints() {
        return getSubstatements(MustDefinition.class);
    }

    private final <T> Set<T> getSubstatements(final Class<T> type) {
        return streamSubstatements(type).collect(ImmutableSet.toImmutableSet());
    }

    private final <T> Stream<T> streamSubstatements(final Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).map(type::cast);
    }
}
