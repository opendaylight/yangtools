/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class IdentityEffectiveStatementImpl extends AbstractEffectiveSchemaNode<IdentityStatement>
        implements IdentitySchemaNode {
    private Set<IdentitySchemaNode> baseIdentity;
    private final Set<IdentitySchemaNode> derivedIdentities;

    public IdentityEffectiveStatementImpl(
            final StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> ctx) {
        super(ctx);

        this.baseIdentity = ImmutableSet.of();
        // initDerivedIdentities
        Set<IdentitySchemaNode> derivedIdentitiesInit = new HashSet<>();
        List<StmtContext<?, ?, ?>> derivedIdentitiesCtxList = ctx.getFromNamespace(
                DerivedIdentitiesNamespace.class, ctx.getStatementArgument());
        if (derivedIdentitiesCtxList == null) {
            this.derivedIdentities = ImmutableSet.of();
            return;
        }
        for (StmtContext<?, ?, ?> derivedIdentityCtx : derivedIdentitiesCtxList) {
            IdentityEffectiveStatementImpl derivedIdentity = (IdentityEffectiveStatementImpl) derivedIdentityCtx
                    .buildEffective();
            derivedIdentity.initBaseIdentity(this);
            derivedIdentitiesInit.add(derivedIdentity);
        }
        this.derivedIdentities = ImmutableSet.copyOf(derivedIdentitiesInit);
    }

    private void initBaseIdentity(final IdentityEffectiveStatementImpl baseIdentity) {
        this.baseIdentity = ImmutableSet.of(baseIdentity);
    }

    @Override
    public Set<IdentitySchemaNode> getBaseIdentities() {
        return baseIdentity;
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
        IdentityEffectiveStatementImpl other = (IdentityEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return IdentityEffectiveStatementImpl.class.getSimpleName() + "[" +
                "base=" + baseIdentity +
                ", qname=" + getQName() +
                "]";
    }
}
