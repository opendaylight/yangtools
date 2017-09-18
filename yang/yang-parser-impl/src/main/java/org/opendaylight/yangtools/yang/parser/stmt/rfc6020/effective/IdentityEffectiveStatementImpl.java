/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class IdentityEffectiveStatementImpl extends AbstractEffectiveSchemaNode<IdentityStatement> implements
        IdentitySchemaNode, MutableStatement {
    private final Set<IdentitySchemaNode> derivedIdentities;
    private Set<IdentitySchemaNode> baseIdentities;
    private boolean sealed;

    public IdentityEffectiveStatementImpl(
            final StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> ctx) {
        super(ctx);

        this.baseIdentities = new HashSet<>();
        ((StmtContext.Mutable<?, ?, ?>) ctx).addMutableStmtToSeal(this);

        // initDerivedIdentities
        final Set<IdentitySchemaNode> derivedIdentitiesInit = new HashSet<>();
        final List<StmtContext<?, ?, ?>> derivedIdentitiesCtxList = ctx.getFromNamespace(
                DerivedIdentitiesNamespace.class, ctx.getStatementArgument());
        if (derivedIdentitiesCtxList == null) {
            this.derivedIdentities = ImmutableSet.of();
            return;
        }
        for (final StmtContext<?, ?, ?> derivedIdentityCtx : derivedIdentitiesCtxList) {
            final IdentityEffectiveStatementImpl derivedIdentity = (IdentityEffectiveStatementImpl) derivedIdentityCtx
                    .buildEffective();
            derivedIdentity.addBaseIdentity(this);
            derivedIdentitiesInit.add(derivedIdentity);
        }
        this.derivedIdentities = ImmutableSet.copyOf(derivedIdentitiesInit);
    }

    private void addBaseIdentity(final IdentityEffectiveStatementImpl baseIdentity) {
        Preconditions.checkState(!sealed, "Attempt to modify sealed identity effective statement %s", getQName());
        this.baseIdentities.add(baseIdentity);
    }

    @Nonnull
    @Override
    public Set<IdentitySchemaNode> getBaseIdentities() {
        Preconditions.checkState(sealed,
                "Attempt to get base identities from unsealed identity effective statement %s", getQName());
        return baseIdentities;
    }

    @Override
    public Set<IdentitySchemaNode> getDerivedIdentities() {
        return Collections.unmodifiableSet(derivedIdentities);
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
        final IdentityEffectiveStatementImpl other = (IdentityEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).toString();
    }

    @Override
    public void seal() {
        if (!sealed) {
            baseIdentities = ImmutableSet.copyOf(baseIdentities);
            sealed = true;
        }
    }
}
