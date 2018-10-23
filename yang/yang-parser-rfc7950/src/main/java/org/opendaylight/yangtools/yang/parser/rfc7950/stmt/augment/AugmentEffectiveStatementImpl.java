/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

final class AugmentEffectiveStatementImpl
        extends AbstractEffectiveDocumentedDataNodeContainer<SchemaNodeIdentifier, AugmentStatement>
        implements AugmentEffectiveStatement, AugmentationSchemaNode, NamespaceRevisionAware {
    private final SchemaPath targetPath;
    private final URI namespace;
    private final Revision revision;
    private final Set<ActionDefinition> actions;
    private final Set<NotificationDefinition> notifications;
    private final @NonNull List<UnknownSchemaNode> unknownNodes;
    private final RevisionAwareXPath whenCondition;
    private final AugmentationSchemaNode copyOf;

    AugmentEffectiveStatementImpl(final StmtContext<SchemaNodeIdentifier, AugmentStatement,
            EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
        super(ctx);

        this.targetPath = ctx.getStatementArgument().asSchemaPath();

        final QNameModule rootModuleQName = StmtContextUtils.getRootModuleQName(ctx);
        this.namespace = rootModuleQName.getNamespace();
        this.revision = rootModuleQName.getRevision().orElse(null);

        this.copyOf = (AugmentationSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        whenCondition = findFirstEffectiveSubstatementArgument(WhenEffectiveStatement.class).orElse(null);

        // initSubstatementCollections
        final ImmutableSet.Builder<ActionDefinition> actionsBuilder = ImmutableSet.builder();
        final ImmutableSet.Builder<NotificationDefinition> notificationsBuilder = ImmutableSet.builder();
        final ImmutableList.Builder<UnknownSchemaNode> listBuilder = new ImmutableList.Builder<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof ActionDefinition) {
                actionsBuilder.add((ActionDefinition) effectiveStatement);
            } else if (effectiveStatement instanceof NotificationDefinition) {
                notificationsBuilder.add((NotificationDefinition) effectiveStatement);
            } else if (effectiveStatement instanceof UnknownSchemaNode) {
                listBuilder.add((UnknownSchemaNode) effectiveStatement);
            }
        }

        this.actions = actionsBuilder.build();
        this.notifications = notificationsBuilder.build();
        this.unknownNodes = listBuilder.build();
    }

    @Override
    public Optional<AugmentationSchemaNode> getOriginalDefinition() {
        return Optional.ofNullable(this.copyOf);
    }

    @Override
    public SchemaPath getTargetPath() {
        return targetPath;
    }

    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.ofNullable(whenCondition);
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public URI getNamespace() {
        return namespace;
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
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + Objects.hashCode(targetPath);
        result = prime * result + Objects.hashCode(whenCondition);
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
        return Objects.equals(targetPath, other.targetPath) && Objects.equals(whenCondition, other.whenCondition)
                && getChildNodes().equals(other.getChildNodes());
    }

    @Override
    public String toString() {
        return AugmentEffectiveStatementImpl.class.getSimpleName() + "[" + "targetPath=" + targetPath + ", when="
                + whenCondition + "]";
    }
}
