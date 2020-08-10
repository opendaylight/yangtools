/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ThirdPartyExtensionSupport
        extends AbstractStatementSupport<String, ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> {

    private static final ThirdPartyExtensionSupport INSTANCE = new ThirdPartyExtensionSupport();

    private ThirdPartyExtensionSupport() {
        super(ThirdPartyExtensionsMapping.THIRD_PARTY_EXTENSION);
    }

    public static ThirdPartyExtensionSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<String, ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.addToNs(ThirdPartyNamespace.class, stmt, "Third-party namespace test.");
    }

    @Override
    public ThirdPartyExtensionStatement createDeclared(final StmtContext<String, ThirdPartyExtensionStatement, ?> ctx) {
        return new ThirdPartyExtensionStatementImpl(ctx);
    }

    @Override
    public ThirdPartyExtensionEffectiveStatement createEffective(
            final StmtContext<String, ThirdPartyExtensionStatement, ThirdPartyExtensionEffectiveStatement> ctx) {
        return new ThirdPartyExtensionEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return null;
    }
}