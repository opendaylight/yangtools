/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.Collection;
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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.ActionNodeContainerCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.compat.NotificationNodeContainerCompat;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

final class AugmentEffectiveStatementImpl
        extends AbstractEffectiveDocumentedDataNodeContainer<SchemaNodeIdentifier, AugmentStatement>
        implements AugmentEffectiveStatement, AugmentationSchemaNode, NamespaceRevisionAware,
            ActionNodeContainerCompat<SchemaNodeIdentifier, AugmentStatement>,
            NotificationNodeContainerCompat<SchemaNodeIdentifier, AugmentStatement> {
    private final SchemaPath targetPath;
    private final QNameModule rootModuleQName;
    private final @NonNull ImmutableSet<ActionDefinition> actions;
    private final @NonNull ImmutableSet<NotificationDefinition> notifications;
    private final RevisionAwareXPath whenCondition;
    private final AugmentationSchemaNode copyOf;

    AugmentEffectiveStatementImpl(final StmtContext<SchemaNodeIdentifier, AugmentStatement,
            EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
        super(ctx);
        targetPath = ctx.coerceStatementArgument().asSchemaPath();
        rootModuleQName = StmtContextUtils.getRootModuleQName(ctx);
        copyOf = (AugmentationSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        whenCondition = findFirstEffectiveSubstatementArgument(WhenEffectiveStatement.class).orElse(null);

        // initSubstatementCollections
        final ImmutableSet.Builder<ActionDefinition> actionsBuilder = ImmutableSet.builder();
        final ImmutableSet.Builder<NotificationDefinition> notificationsBuilder = ImmutableSet.builder();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof ActionDefinition) {
                actionsBuilder.add((ActionDefinition) effectiveStatement);
            } else if (effectiveStatement instanceof NotificationDefinition) {
                notificationsBuilder.add((NotificationDefinition) effectiveStatement);
            }
        }

        actions = actionsBuilder.build();
        notifications = notificationsBuilder.build();
    }

    @Override
    protected Collection<? extends EffectiveStatement<?, ?>> initSubstatements(
            final StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx,
            final Collection<? extends StmtContext<?, ?, ?>> substatementsInit) {
        final StatementContextBase<?, ?, ?> implicitDef = ctx.getFromNamespace(AugmentImplicitHandlingNamespace.class,
            ctx);
        return implicitDef == null ? super.initSubstatements(ctx, substatementsInit)
                : Collections2.transform(Collections2.filter(substatementsInit,
                    StmtContext::isSupportedToBuildEffective),
                    subCtx -> {
                        verify(subCtx instanceof StatementContextBase);
                        return implicitDef.wrapWithImplicit((StatementContextBase<?, ?, ?>) subCtx).buildEffective();
                    });
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
    public URI getNamespace() {
        return rootModuleQName.getNamespace();
    }

    @Override
    public Optional<Revision> getRevision() {
        return rootModuleQName.getRevision();
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
