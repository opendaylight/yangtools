/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class IdentityEffectiveStatementImpl extends AbstractEffectiveSchemaNode<IdentityStatement>
        implements IdentityEffectiveStatement, IdentitySchemaNode {
    private final @NonNull Set<IdentitySchemaNode> baseIdentities;

    IdentityEffectiveStatementImpl(final StmtContext<QName, IdentityStatement, IdentityEffectiveStatement> ctx) {
        super(ctx);

        final Set<IdentitySchemaNode> tmp = new HashSet<>();
        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof IdentitySchemaNode) {
                tmp.add((IdentitySchemaNode) stmt);
            }
        }
        this.baseIdentities = ImmutableSet.copyOf(tmp);
    }

    @Override
    public Collection<? extends IdentitySchemaNode> getBaseIdentities() {
        return baseIdentities;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).toString();
    }
}
