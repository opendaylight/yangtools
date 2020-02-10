/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RegularIdentityEffectiveStatement extends AbstractIdentityEffectiveStatement {
    private final @NonNull Object baseIdentities;
    private final @NonNull Object substatements;
    private final int flags;

    RegularIdentityEffectiveStatement(final IdentityStatement declared,
            final StmtContext<QName, IdentityStatement, IdentityEffectiveStatement> ctx, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableSet<? extends IdentitySchemaNode> baseIdentities) {
        super(declared, ctx);
        this.flags = flags;
        this.substatements = maskList(substatements);
        this.baseIdentities = maskSet(baseIdentities);
    }

    @Override
    public Collection<? extends IdentitySchemaNode> getBaseIdentities() {
        return unmaskSet(baseIdentities, IdentitySchemaNode.class);
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }
}
