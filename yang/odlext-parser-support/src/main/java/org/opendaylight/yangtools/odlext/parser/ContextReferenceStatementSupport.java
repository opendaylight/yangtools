/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceStatement;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public final class ContextReferenceStatementSupport
        extends AbstractIdentityAwareStatementSupport<ContextReferenceStatement, ContextReferenceEffectiveStatement> {
    public static final @NonNull ContextReferenceStatementSupport INSTANCE = new ContextReferenceStatementSupport();

    private ContextReferenceStatementSupport() {
        super(OpenDaylightExtensionsStatements.CONTEXT_REFERENCE);
    }

    @Override
    protected ContextReferenceStatement createDeclared(final StmtContext<QName, ContextReferenceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ContextReferenceStatementImpl(ctx.rawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    ContextReferenceEffectiveStatement createEffective(final ContextReferenceStatement declared,
            final IdentityEffectiveStatement identity,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ContextReferenceEffectiveStatementImpl(declared, identity, substatements);
    }
}
