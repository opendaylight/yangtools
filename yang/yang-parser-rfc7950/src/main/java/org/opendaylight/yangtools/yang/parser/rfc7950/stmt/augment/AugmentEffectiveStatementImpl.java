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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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
    private static final VarHandle ACTIONS;
    private static final VarHandle NOTIFICATIONS;

    static {
        final Lookup lookup = MethodHandles.lookup();

        try {
            ACTIONS = lookup.findVarHandle(AugmentEffectiveStatementImpl.class, "actions", ImmutableSet.class);
            NOTIFICATIONS = lookup.findVarHandle(AugmentEffectiveStatementImpl.class, "notifications",
                ImmutableSet.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final SchemaPath targetPath;
    private final URI namespace;
    private final Revision revision;
    private final RevisionAwareXPath whenCondition;
    private final AugmentationSchemaNode copyOf;

    @SuppressWarnings("unused")
    private volatile ImmutableSet<ActionDefinition> actions;
    @SuppressWarnings("unused")
    private volatile ImmutableSet<NotificationDefinition> notifications;

    AugmentEffectiveStatementImpl(final StmtContext<SchemaNodeIdentifier, AugmentStatement,
            EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
        super(ctx);

        this.targetPath = ctx.coerceStatementArgument().asSchemaPath();

        final QNameModule rootModuleQName = StmtContextUtils.getRootModuleQName(ctx);
        this.namespace = rootModuleQName.getNamespace();
        this.revision = rootModuleQName.getRevision().orElse(null);

        this.copyOf = (AugmentationSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        whenCondition = findFirstEffectiveSubstatementArgument(WhenEffectiveStatement.class).orElse(null);
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
        return namespace;
    }

    @Override
    public ImmutableSet<ActionDefinition> getActions() {
        return derivedSet(ACTIONS, ActionDefinition.class);
    }

    @Override
    public ImmutableSet<NotificationDefinition> getNotifications() {
        return derivedSet(NOTIFICATIONS, NotificationDefinition.class);
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
