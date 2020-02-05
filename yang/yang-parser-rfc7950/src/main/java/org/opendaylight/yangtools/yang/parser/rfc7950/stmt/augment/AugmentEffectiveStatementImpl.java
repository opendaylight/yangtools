/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.WhenConditionMixin;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class AugmentEffectiveStatementImpl extends DefaultDataNodeContainer<SchemaNodeIdentifier, AugmentStatement>
        implements AugmentEffectiveStatement, AugmentationSchemaNode, NamespaceRevisionAware,
            DocumentedNodeMixin.WithStatus<SchemaNodeIdentifier, AugmentStatement>,
            DataNodeContainerMixin<SchemaNodeIdentifier, AugmentStatement>,
            ActionNodeContainerMixin<SchemaNodeIdentifier, AugmentStatement>,
            ActionNodeContainerCompat<SchemaNodeIdentifier, AugmentStatement>,
            NotificationNodeContainerMixin<SchemaNodeIdentifier, AugmentStatement>,
            NotificationNodeContainerCompat<SchemaNodeIdentifier, AugmentStatement>,
            WhenConditionMixin<SchemaNodeIdentifier, AugmentStatement> {
    private final @Nullable AugmentationSchemaNode original;
    private final @NonNull QNameModule rootModuleQName;
    private final int flags;

    AugmentEffectiveStatementImpl(final AugmentStatement declared, final int flags, final QNameModule rootModuleQName,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final StatementSourceReference ref,
            final @Nullable AugmentationSchemaNode original) {
        super(declared, ref, substatements);
        this.rootModuleQName = requireNonNull(rootModuleQName);
        this.flags = flags;
        this.original = original;
    }

    @Override
    public SchemaNodeIdentifier argument() {
        return getDeclared().argument();
    }

    @Override
    public Optional<AugmentationSchemaNode> getOriginalDefinition() {
        return Optional.ofNullable(this.original);
    }

    @Override
    public SchemaPath getTargetPath() {
        return argument().asSchemaPath();
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public URI getNamespace() {
        return rootModuleQName.getNamespace();
    }

    @Override
    public Optional<Revision> getRevision() {
        return rootModuleQName.getRevision();
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + Objects.hashCode(getTargetPath());
        result = prime * result + Objects.hashCode(getWhenCondition());
        result = prime * result + getChildNodes().hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AugmentEffectiveStatementImpl)) {
            return false;
        }
        final AugmentEffectiveStatementImpl other = (AugmentEffectiveStatementImpl) obj;
        return Objects.equals(getTargetPath(), other.getTargetPath())
                && Objects.equals(getWhenCondition(), other.getWhenCondition())
                && getChildNodes().equals(other.getChildNodes());
    }

    @Override

    public String toString() {
        return AugmentEffectiveStatementImpl.class.getSimpleName() + "[" + "targetPath=" + getTargetPath() + ", when="
                + getWhenCondition() + "]";
    }
}
