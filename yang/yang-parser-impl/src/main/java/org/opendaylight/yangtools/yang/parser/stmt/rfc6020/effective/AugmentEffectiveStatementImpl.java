/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

public final class AugmentEffectiveStatementImpl extends
        AbstractEffectiveDocumentedDataNodeContainer<SchemaNodeIdentifier, AugmentStatement> implements
        AugmentationSchema, NamespaceRevisionAware, Comparable<AugmentEffectiveStatementImpl> {
    private final SchemaPath targetPath;
    private final URI namespace;
    private final Date revision;
    private final int order;
    private final Set<ActionDefinition> actions;
    private final Set<NotificationDefinition> notifications;
    private final List<UnknownSchemaNode> unknownNodes;
    private final RevisionAwareXPath whenCondition;
    private final AugmentationSchema copyOf;

    public AugmentEffectiveStatementImpl(
            final StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
        super(ctx);

        this.targetPath = ctx.getStatementArgument().asSchemaPath();

        final QNameModule rootModuleQName = StmtContextUtils.getRootModuleQName(ctx);
        this.namespace = rootModuleQName.getNamespace();
        this.revision = rootModuleQName.getRevision();

        this.order = ctx.getOrder();
        this.copyOf = (AugmentationSchema) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        final WhenEffectiveStatementImpl whenStmt = firstEffective(WhenEffectiveStatementImpl.class);
        this.whenCondition = whenStmt == null ? null : whenStmt.argument();

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
    public Optional<AugmentationSchema> getOriginalDefinition() {
        return Optional.fromNullable(this.copyOf);
    }

    @Override
    public SchemaPath getTargetPath() {
        return targetPath;
    }

    @Override
    public RevisionAwareXPath getWhenCondition() {
        return whenCondition;
    }

    @Nonnull
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
    public Date getRevision() {
        return revision;
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AugmentEffectiveStatementImpl other = (AugmentEffectiveStatementImpl) obj;
        if (!Objects.equals(targetPath, other.targetPath)) {
            return false;
        }
        if (!Objects.equals(whenCondition, other.whenCondition)) {
            return false;
        }
        if (!getChildNodes().equals(other.getChildNodes())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return AugmentEffectiveStatementImpl.class.getSimpleName() + "[" + "targetPath=" + targetPath + ", when="
                + whenCondition + "]";
    }

    @Override
    public int compareTo(@Nonnull final AugmentEffectiveStatementImpl o) {
        checkNotNull(o);
        final Iterator<QName> thisIt = this.targetPath.getPathFromRoot().iterator();
        final Iterator<QName> otherIt = o.getTargetPath().getPathFromRoot().iterator();
        while (thisIt.hasNext()) {
            if (otherIt.hasNext()) {
                final int comp = thisIt.next().compareTo(otherIt.next());
                if (comp != 0) {
                    return comp;
                }
            } else {
                return 1;
            }
        }
        if (otherIt.hasNext()) {
            return -1;
        }
        return this.order - o.order;
    }
}
