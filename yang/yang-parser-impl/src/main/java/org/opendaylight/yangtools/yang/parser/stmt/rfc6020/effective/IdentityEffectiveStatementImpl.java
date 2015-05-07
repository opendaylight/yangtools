/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;

import java.util.HashSet;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class IdentityEffectiveStatementImpl extends
        AbstractEffectiveDocumentedNode<QName, IdentityStatement> implements
        IdentitySchemaNode {
    private final QName qname;
    private final SchemaPath path;
    IdentitySchemaNode baseIdentity;
    private ImmutableSet<IdentitySchemaNode> derivedIdentities;

    ImmutableList<UnknownSchemaNode> unknownNodes;

    public IdentityEffectiveStatementImpl(
            StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> ctx) {
        super(ctx);

        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);

        initSubstatementCollections();
        initDerivedIdentities(ctx);
    }

    private void initDerivedIdentities(
            StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> ctx) {

        Set<IdentitySchemaNode> derivedIdentitiesInit = new HashSet<IdentitySchemaNode>();
        List<StmtContext<?, ?, ?>> derivedIdentitiesCtxList = ctx.getFromNamespace(
                DerivedIdentitiesNamespace.class, ctx.getStatementArgument());

        if(derivedIdentitiesCtxList == null) {
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

    private void initBaseIdentity(IdentityEffectiveStatementImpl baseIdentity) {
        this.baseIdentity = baseIdentity;
    }

    private void initSubstatementCollections() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public IdentitySchemaNode getBaseIdentity() {
        return baseIdentity;
    }

    @Override
    public Set<IdentitySchemaNode> getDerivedIdentities() {
        return Collections.unmodifiableSet(derivedIdentities);
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qname == null) ? 0 : qname.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        if (qname == null) {
            if (other.qname != null) {
                return false;
            }
        } else if (!qname.equals(other.qname)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                IdentityEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("base=").append(baseIdentity);
        sb.append(", qname=").append(qname);
        sb.append("]");
        return sb.toString();
    }
}