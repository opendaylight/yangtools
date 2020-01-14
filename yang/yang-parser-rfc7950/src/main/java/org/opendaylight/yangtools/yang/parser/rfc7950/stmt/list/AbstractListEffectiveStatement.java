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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
            ActionNodeContainerCompat<QName, ListStatement>, NotificationNodeContainerCompat<QName, ListStatement>,
            DataSchemaNodeMixin<QName, ListStatement>, UserOrderedMixin<QName, ListStatement>,
            DataNodeContainerMixin<QName, ListStatement>, WhenConditionMixin<QName, ListStatement>,
            AugmentationTargetMixin<QName, ListStatement>, NotificationNodeContainerMixin<QName, ListStatement>,
            ActionNodeContainerMixin<QName, ListStatement>, MustConstraintMixin<QName, ListStatement> {
    private final int flags;
    // Variable: either a single substatement or an ImmutableList
    private final @NonNull Object substatements;
    private final @NonNull SchemaPath path;
    // Variable: either a single QName or an ImmutableList
    private final @NonNull Object keyDefinition;

    AbstractListEffectiveStatement(final ListStatement declared, final SchemaPath path, final int flags,
            final StmtContext<?, ?, ?> ctx, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<QName> keyDefinition) {
        super(declared, ctx, substatements);

        EffectiveStmtUtils.checkUniqueGroupings(ctx, substatements);
        EffectiveStmtUtils.checkUniqueTypedefs(ctx, substatements);
        EffectiveStmtUtils.checkUniqueUses(ctx, substatements);

        this.substatements = substatements.size() == 1 ? substatements.get(0) : substatements;
        this.path = requireNonNull(path);
        this.keyDefinition = keyDefinition.size() == 1 ? keyDefinition.get(0) : keyDefinition;
        this.flags = flags;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return (ImmutableList) listFrom(substatements, EffectiveStatement.class);
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
    public final List<QName> getKeyDefinition() {
        return listFrom(keyDefinition, QName.class);
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
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(getPath());
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractListEffectiveStatement)) {
            return false;
        }
        final AbstractListEffectiveStatement other = (AbstractListEffectiveStatement) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public final String toString() {
        return "list " + getQName().getLocalName();
    }

    @SuppressWarnings("unchecked")
    private static <T> @NonNull ImmutableList<T> listFrom(final Object obj, final Class<T> type) {
        if (obj instanceof ImmutableList) {
            return (ImmutableList<T>) obj;
        }
        verify(type.isInstance(obj), "Unexpected list value %s", obj);
        return ImmutableList.of(type.cast(obj));
    }
}
