/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ThirdPartyExtensionSupport
        extends BaseStringStatementSupport<ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> {

    private static final ThirdPartyExtensionSupport INSTANCE = new ThirdPartyExtensionSupport();

    private ThirdPartyExtensionSupport() {
        super(ThirdPartyExtensionsMapping.THIRD_PARTY_EXTENSION);
    }

    public static ThirdPartyExtensionSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<String, ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.addToNs(ThirdPartyNamespace.class, stmt, "Third-party namespace test.");
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return null;
    }

    @Override
    protected ThirdPartyExtensionStatement createDeclared(
            final StmtContext<String, ThirdPartyExtensionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ThirdPartyExtensionStatementImpl(ctx, substatements);
    }

    @Override
    protected ThirdPartyExtensionStatement createEmptyDeclared(
            final StmtContext<String, ThirdPartyExtensionStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected ThirdPartyExtensionEffectiveStatement createEffective(
            final StmtContext<String, ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> ctx,
            final ThirdPartyExtensionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ThirdPartyExtensionEffectiveStatementImpl(ctx, substatements);
    }

    @Override
    protected ThirdPartyExtensionEffectiveStatement createEmptyEffective(
            final StmtContext<String, ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> ctx,
            final ThirdPartyExtensionStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
