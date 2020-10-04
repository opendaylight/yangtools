/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithDataTree;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.UserOrderedMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.WhenConditionMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractListEffectiveStatement
        extends DefaultWithDataTree<QName, ListStatement, ListEffectiveStatement>
        implements ListEffectiveStatement, ListSchemaNode, DerivableSchemaNode,
            ActionNodeContainerCompat<QName, ListStatement, ListEffectiveStatement>,
            NotificationNodeContainerCompat<QName, ListStatement, ListEffectiveStatement>,
            DataSchemaNodeMixin<QName, ListStatement>, UserOrderedMixin<QName, ListStatement>,
            DataNodeContainerMixin<QName, ListStatement>, WhenConditionMixin<QName, ListStatement>,
            AugmentationTargetMixin<QName, ListStatement>, NotificationNodeContainerMixin<QName, ListStatement>,
            ActionNodeContainerMixin<QName, ListStatement>, MustConstraintMixin<QName, ListStatement> {
    private final int flags;
    private final @NonNull Object substatements;
    private final @NonNull SchemaPath path;
    private final @NonNull Object keyDefinition;

    AbstractListEffectiveStatement(final ListStatement declared, final SchemaPath path, final int flags,
            final StmtContext<?, ?, ?> ctx, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<QName> keyDefinition) {
        super(declared, ctx, substatements);

        EffectiveStmtUtils.checkUniqueGroupings(ctx, substatements);
        EffectiveStmtUtils.checkUniqueTypedefs(ctx, substatements);
        EffectiveStmtUtils.checkUniqueUses(ctx, substatements);

        this.substatements = maskList(substatements);
        this.path = requireNonNull(path);
        this.keyDefinition = maskList(keyDefinition);
        this.flags = flags;
    }

    @Override
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
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
    @Deprecated
    public final SchemaPath getPath() {
        return path;
    }

    @Override
    public final boolean isUserOrdered() {
        return userOrdered();
    }

    @Override
    public final List<QName> getKeyDefinition() {
        return unmaskList(keyDefinition, QName.class);
    }

    @Override
    public final Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return findDataSchemaNode(name);
    }

    @Override
    public final Collection<UniqueConstraint> getUniqueConstraints() {
        return effectiveSubstatements().stream()
                .filter(UniqueConstraint.class::isInstance)
                .map(UniqueConstraint.class::cast)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public final ListEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public final String toString() {
        return "list " + getQName().getLocalName();
    }
}
