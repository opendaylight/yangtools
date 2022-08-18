/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

public final class ThirdPartyExtensionSupport
        extends AbstractStringStatementSupport<ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> {
    public ThirdPartyExtensionSupport(final YangParserConfiguration config) {
        super(ThirdPartyExtensionsMapping.THIRD_PARTY_EXTENSION, StatementPolicy.contextIndependent(), config, null);
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<String, ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.addToNs(ThirdPartyNamespace.INSTANCE, Empty.value(), "Third-party namespace test.");
    }

    @Override
    protected ThirdPartyExtensionStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new ThirdPartyExtensionStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected ThirdPartyExtensionStatement attachDeclarationReference(final ThirdPartyExtensionStatement stmt,
            final DeclarationReference reference) {
        return new RefThirdPartyExtensionStatement(stmt, reference);
    }

    @Override
    protected ThirdPartyExtensionEffectiveStatement createEffective(
            final Current<String, ThirdPartyExtensionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ThirdPartyExtensionEffectiveStatementImpl(stmt, substatements);
    }
}
