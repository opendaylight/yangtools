/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.thirdparty.plugin;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

public class ThirdPartyExtensionStatementImpl extends AbstractDeclaredStatement<String> implements
        UnknownStatement<String> {

    ThirdPartyExtensionStatementImpl(final StmtContext<String, UnknownStatement<String>, ?> context) {
        super(context);
    }

    public static class ThirdPartyExtensionSupport
            extends
            AbstractStatementSupport<String, UnknownStatement<String>, EffectiveStatement<String, UnknownStatement<String>>> {

        public ThirdPartyExtensionSupport() {
            super(ThirdPartyExtensionsMapping.THIRD_PARTY_EXTENSION);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public void onFullDefinitionDeclared(
                final Mutable<String, UnknownStatement<String>, EffectiveStatement<String, UnknownStatement<String>>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            stmt.addToNs(ThirdPartyNamespace.class, stmt, "Third-party namespace test.");
        }

        @Override
        public UnknownStatement<String> createDeclared(final StmtContext<String, UnknownStatement<String>, ?> ctx) {
            return new ThirdPartyExtensionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, UnknownStatement<String>> createEffective(
                final StmtContext<String, UnknownStatement<String>, EffectiveStatement<String, UnknownStatement<String>>> ctx) {
            return new ThirdPartyExtensionEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return null;
        }
    }

    @Override
    public String getArgument() {
        return argument();
    }
}
