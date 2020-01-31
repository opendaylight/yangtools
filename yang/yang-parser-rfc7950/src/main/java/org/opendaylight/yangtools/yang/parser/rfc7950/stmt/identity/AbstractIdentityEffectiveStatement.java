/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin.WithStatus;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractIdentityEffectiveStatement extends DefaultArgument<QName, IdentityStatement>
        implements IdentityEffectiveStatement, IdentitySchemaNode, MutableStatement,
                   WithStatus<QName, IdentityStatement> {
    private final @NonNull SchemaPath path;
    private final ImmutableSet<IdentitySchemaNode> derivedIdentities;
    private @NonNull Set<IdentitySchemaNode> baseIdentities;
    private boolean sealed;

    AbstractIdentityEffectiveStatement(final IdentityStatement declared,
            final StmtContext<QName, IdentityStatement, IdentityEffectiveStatement> ctx) {
        super(declared);
        this.path = ctx.getSchemaPath().get();

        this.baseIdentities = new HashSet<>();
        ((StmtContext.Mutable<?, ?, ?>) ctx).addMutableStmtToSeal(this);

        // initDerivedIdentities
        final Set<IdentitySchemaNode> derivedIdentitiesInit = new HashSet<>();
        final List<StmtContext<?, ?, ?>> derivedIdentitiesCtxList = ctx.getFromNamespace(
                DerivedIdentitiesNamespace.class, ctx.coerceStatementArgument());
        if (derivedIdentitiesCtxList == null) {
            this.derivedIdentities = ImmutableSet.of();
            return;
        }
        for (final StmtContext<?, ?, ?> derivedIdentityCtx : derivedIdentitiesCtxList) {
            final AbstractIdentityEffectiveStatement derivedIdentity =
                    (AbstractIdentityEffectiveStatement) derivedIdentityCtx.buildEffective();
            derivedIdentity.addBaseIdentity(this);
            derivedIdentitiesInit.add(derivedIdentity);
        }
        this.derivedIdentities = ImmutableSet.copyOf(derivedIdentitiesInit);
    }

    private void addBaseIdentity(final AbstractIdentityEffectiveStatement baseIdentity) {
        checkState(!sealed, "Attempt to modify sealed identity effective statement %s", getQName());
        this.baseIdentities.add(baseIdentity);
    }


    @Override
    public final SchemaPath getPath() {
        return path;
    }

    @Override
    public final Collection<? extends IdentitySchemaNode> getBaseIdentities() {
        checkState(sealed, "Attempt to get base identities from unsealed identity effective statement %s", getQName());
        return baseIdentities;
    }

    @Override
    public final Collection<? extends IdentitySchemaNode> getDerivedIdentities() {
        return Collections.unmodifiableSet(derivedIdentities);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).toString();
    }

    @Override
    public final void seal() {
        if (!sealed) {
            baseIdentities = ImmutableSet.copyOf(baseIdentities);
            sealed = true;
        }
    }
}
