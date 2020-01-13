/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithDataTree;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.PresenceMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ContainerEffectiveStatementImpl
        extends DefaultWithDataTree<QName, ContainerStatement, ContainerEffectiveStatement>
        implements ContainerEffectiveStatement, ContainerSchemaNode, DerivableSchemaNode,
            DataSchemaNodeMixin<QName, ContainerStatement>, DataNodeContainerMixin<QName, ContainerStatement>,
            ActionNodeContainerMixin<QName, ContainerStatement>,
            ActionNodeContainerCompat<QName, ContainerStatement>,
            NotificationNodeContainerMixin<QName, ContainerStatement>,
            NotificationNodeContainerCompat<QName, ContainerStatement>,
            MustConstraintMixin<QName, ContainerStatement>, PresenceMixin<QName, ContainerStatement>,
            AugmentationTargetMixin<QName, ContainerStatement> {

    private final int flags;
    private final @NonNull Object substatements;
    private final @NonNull SchemaPath path;
    private final @Nullable ContainerSchemaNode original;

    ContainerEffectiveStatementImpl(final ContainerStatement declared, final SchemaPath path, final int flags,
                final StmtContext<?, ?, ?> ctx, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
                final ContainerSchemaNode original) {
        super(declared, ctx, substatements);

        EffectiveStmtUtils.checkUniqueGroupings(ctx, substatements);
        EffectiveStmtUtils.checkUniqueTypedefs(ctx, substatements);
        EffectiveStmtUtils.checkUniqueUses(ctx, substatements);

        this.substatements = substatements.size() == 1 ? substatements.get(0) : substatements;
        this.path = requireNonNull(path);
        this.original = original;
        this.flags = flags;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        if (substatements instanceof ImmutableList) {
            return (ImmutableList<? extends EffectiveStatement<?, ?>>) substatements;
        }
        verify(substatements instanceof EffectiveStatement, "Unexpected substatement %s", substatements);
        return ImmutableList.of((EffectiveStatement<?, ?>) substatements);
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QName argument() {
        return getQName();
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public Optional<ContainerSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public boolean isPresenceContainer() {
        return presence();
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return findDataSchemaNode(name);
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
        final ContainerEffectiveStatementImpl other = (ContainerEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return "container " + getQName().getLocalName();
    }
}
